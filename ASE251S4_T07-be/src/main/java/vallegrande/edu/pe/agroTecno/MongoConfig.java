package vallegrande.edu.pe.agroTecno;

import java.time.LocalDate;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;

import vallegrande.edu.pe.agroTecno.model.Formula;

@Configuration
@EnableReactiveMongoRepositories
public class MongoConfig extends AbstractReactiveMongoConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Override
    public MongoClient reactiveMongoClient() {
        return MongoClients.create(mongoUri);
    }

    @Override
    protected String getDatabaseName() {
        if (mongoUri != null && mongoUri.contains("/")) {
            try {
                String path = mongoUri.substring(mongoUri.lastIndexOf("/") + 1);
                if (path.contains("?")) {
                    path = path.substring(0, path.indexOf("?"));
                }
                if (!path.isBlank()) {
                    return path;
                }
            } catch (Exception ignored) {
            }
        }
        return "AgroTecnoDB";
    }

    @Bean
    MongoCustomConversions mongoCustomConversions() {
        return MongoCustomConversions.create(adapter ->
                adapter.registerConverter(new FormulaInventoryDocumentConverter()));
    }

    private static final class FormulaInventoryDocumentConverter
            implements Converter<Document, List<Formula.FormulaInventory>> {

        @Override
        public List<Formula.FormulaInventory> convert(Document source) {
            Formula.FormulaInventory inventory = new Formula.FormulaInventory();
            inventory.setGallonType(asInteger(source.get("gallon_type")));
            inventory.setStockQuantity(asInteger(source.get("stock_quantity")));
            inventory.setLastUpdate(asLocalDate(source.get("last_update")));
            return List.of(inventory);
        }

        private Integer asInteger(Object value) {
            return value == null ? null : ((Number) value).intValue();
        }

        private LocalDate asLocalDate(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof LocalDate date) {
                return date;
            }
            return LocalDate.parse(value.toString());
        }
    }
}
