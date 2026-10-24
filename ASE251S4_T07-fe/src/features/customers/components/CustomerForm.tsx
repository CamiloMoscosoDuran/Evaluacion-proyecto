import React, { useState, useEffect } from "react";
import { Customer, FarmPlot } from "../types/customer.types";
import { Button } from "../../../shared/components/Button";
import { Plus, Trash2 } from "lucide-react";

interface CustomerFormProps {
  initialData?: Customer | null;
  onSubmit: (data: Partial<Customer>) => Promise<void>;
  onCancel: () => void;
  isSaving: boolean;
}

interface FieldErrors { [key: string]: string; }
const ONLY_LETTERS = /^[A-Za-zÁáÉéÍíÓóÚúÑñüÜ\s]+$/;

// Departamentos y provincias del Peru
const DEPARTMENTS = [
  "Amazonas","Ancash","Apurimac","Arequipa","Ayacucho","Cajamarca","Cusco",
  "Huancavelica","Huanuco","Ica","Junin","La Libertad","Lambayeque","Lima",
  "Loreto","Madre de Dios","Moquegua","Pasco","Piura","Puno","San Martin",
  "Tacna","Tumbes","Ucayali"
];

const PROVINCES_BY_DEPT: Record<string,string[]> = {
  "Amazonas": ["Bagua","Bongara","Chachapoyas","Condorcanqui","Luya","Rodriguez de Mendoza","Utcubamba"],
  "Ancash": ["Aija","Antonio Raymondi","Asuncion","Bolognesi","Carhuaz","Carlos Fermin Fitzcarrald","Casma","Corongo","Huaraz","Huari","Huarmey","Huaylas","Mariscal Luzuriaga","Ocros","Pallasca","Pomabamba","Recuay","Santa","Sihuas","Yungay"],
  "Apurimac": ["Abancay","Andahuaylas","Antabamba","Aymaraes","Chincheros","Cotabambas","Grau"],
  "Arequipa": ["Arequipa","Camana","Caraveli","Castilla","Caylloma","Condesuyos","Islay","La Union"],
  "Ayacucho": ["Cangallo","Huamanga","Huanca Sancos","Huanta","La Mar","Lucanas","Parinacochas","Paucar del Sara Sara","Sucre","Victor Fajardo","Vilcas Huaman"],
  "Cajamarca": ["Cajabamba","Cajamarca","Celendin","Chota","Contumaza","Cutervo","Hualgayoc","Jaen","San Ignacio","San Marcos","San Miguel","San Pablo","Santa Cruz"],
  "Cusco": ["Acomayo","Anta","Calca","Canas","Canchis","Chumbivilcas","Cusco","Espinar","La Convencion","Paruro","Paucartambo","Quispicanchi","Urubamba"],
  "Huancavelica": ["Acobamba","Angaraes","Castrovirreyna","Churcampa","Huancavelica","Huaytara","Tayacaja"],
  "Huanuco": ["Ambo","Dos de Mayo","Huacaybamba","Huamalies","Huanuco","Lauricocha","Leoncio Prado","Maranon","Pachitea","Puerto Inca","Yarowilca"],
  "Ica": ["Chincha","Ica","Nazca","Palpa","Pisco"],
  "Junin": ["Chanchamayo","Chupaca","Concepcion","Huancayo","Jauja","Junin","Satipo","Tarma","Yauli"],
  "La Libertad": ["Ascope","Bolivar","Chepen","Gran Chimu","Julcan","Otuzco","Pacasmayo","Pataz","Sanchez Carrion","Santiago de Chuco","Trujillo","Viru"],
  "Lambayeque": ["Chiclayo","Ferrenafe","Lambayeque"],
  "Lima": ["Barranca","Cajatambo","Canta","Canete","Huaral","Huarochiri","Huaura","Lima","Oyon","Yauyos"],
  "Loreto": ["Alto Amazonas","Datem del Maranon","Loreto","Mariscal Ramon Castilla","Maynas","Putumayo","Requena","Ucayali"],
  "Madre de Dios": ["Manu","Tahuamanu","Tambopata"],
  "Moquegua": ["General Sanchez Cerro","Ilo","Mariscal Nieto"],
  "Pasco": ["Daniel Alcides Carrion","Oxapampa","Pasco"],
  "Piura": ["Ayabaca","Huancabamba","Morropon","Paita","Piura","Sechura","Sullana","Talara"],
  "Puno": ["Azangaro","Carabaya","Chucuito","El Collao","Huancane","Lampa","Melgar","Moho","Puno","San Antonio de Putina","San Roman","Sandia","Yunguyo"],
  "San Martin": ["Bellavista","El Dorado","Huallaga","Lamas","Mariscal Caceres","Moyobamba","Picota","Rioja","San Martin","Tocache"],
  "Tacna": ["Candarave","Jorge Basadre","Tacna","Tarata"],
  "Tumbes": ["Contralmirante Villar","Tumbes","Zarumilla"],
  "Ucayali": ["Atalaya","Coronel Portillo","Padre Abad","Purus"]
};

const SETTLEMENT_TYPES = ["Caserio","Comunidad campesina","Fundo","Hacienda","Campamento","Anexo","Sector","Urbanizacion","Asentamiento humano","Asociacion","Condominio"];
const STREET_TYPES = ["Carretera","Trocha","Camino","Pasaje","Avenida","Jiron","Calle","Ovalo","Plaza"];

export const CustomerForm: React.FC<CustomerFormProps> = ({ initialData, onSubmit, onCancel, isSaving }) => {
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [documentType, setDocumentType] = useState("DNI");
  const [documentNumber, setDocumentNumber] = useState("");
  const [phoneString, setPhoneString] = useState("");
  const [email, setEmail] = useState("");
  const [registrationDate] = useState(() => new Date().toISOString().split("T")[0]);
  const [farmPlots, setFarmPlots] = useState<FarmPlot[]>([]);

  const [department, setDepartment] = useState("Ica");
  const [province, setProvince] = useState("");
  const [district, setDistrict] = useState("");
  const [settlementType, setSettlementType] = useState("Fundo");
  const [settlementName, setSettlementName] = useState("");
  const [streetType, setStreetType] = useState("Carretera");
  const [streetName, setStreetName] = useState("");
  const [reference, setReference] = useState("");
  const [errors, setErrors] = useState<FieldErrors>({});

  const provinces = PROVINCES_BY_DEPT[department] || [];

  // Reset province/district when department changes
  useEffect(() => {
    const prvs = PROVINCES_BY_DEPT[department] || [];
    if (!prvs.includes(province)) {
      setProvince(prvs[0] || "");
      setDistrict("");
    }
  }, [department]);

  useEffect(() => {
    if (initialData) {
      setFirstName(initialData.first_name || "");
      setLastName(initialData.last_name || "");
      setDocumentType(initialData.document_type || "DNI");
      setDocumentNumber(initialData.document_number || "");
      setPhoneString(initialData.phone ? initialData.phone.join(", ") : "");
      setEmail(initialData.email || "");
      if (initialData.farm_plot && initialData.farm_plot.length > 0) setFarmPlots(initialData.farm_plot.map((fp) => ({ ...fp, hectareas: parseFloat(String(fp.hectareas)) || 0 })));
      if (initialData.address) {
        setDepartment(initialData.address.department || "Ica");
        setProvince(initialData.address.province || "");
        setDistrict(initialData.address.district || "");
        if (initialData.address.especifics) {
          setSettlementType(initialData.address.especifics.settlement_type || "Fundo");
          setSettlementName(initialData.address.especifics.settlement_name || "");
          setStreetType(initialData.address.especifics.street_type || "Carretera");
          setStreetName(initialData.address.especifics.street_name || "");
          setReference(initialData.address.especifics.reference || "");
        }
      }
    }
  }, [initialData]);

  const addFarmPlot = () => setFarmPlots([...farmPlots, { farm_name: "", hectareas: 1, type_crop: "" }]);
  const removeFarmPlot = (i: number) => setFarmPlots(farmPlots.filter((_, idx) => idx !== i));
    const updateFarmPlot = (i: number, f: keyof FarmPlot, v: string | number) => {
    const updated = [...farmPlots];
    // Forzar conversion numerica para campos numericos
    const coerced = f === "hectareas" ? parseFloat(String(v)) || 0 : v;
    updated[i] = { ...updated[i], [f]: coerced };
    setFarmPlots(updated);
  };

  const clearError = (f: string) => setErrors((prev) => ({ ...prev, [f]: "" }));

  const getDocNumberPattern = () => {
    if (documentType === "DNI") return "^[0-9]{8}$";
    if (documentType === "RUC") return "^[0-9]{11}$";
    return "^[0-9]{9}$";
  };

  const validate = (): boolean => {
    const e: FieldErrors = {};
    if (!firstName.trim()) e.firstName = "El nombre es obligatorio.";
    else if (firstName.trim().length < 3) e.firstName = "El nombre debe tener al menos 3 caracteres.";
    else if (!ONLY_LETTERS.test(firstName.trim())) e.firstName = "El nombre debe contener solo letras.";

    if (!lastName.trim()) e.lastName = "Los apellidos son obligatorios.";
    else if (lastName.trim().length < 3) e.lastName = "Los apellidos deben tener al menos 3 caracteres.";
    else if (!ONLY_LETTERS.test(lastName.trim())) e.lastName = "Los apellidos deben contener solo letras.";

    const docNum = documentNumber.trim();
    if (!docNum) e.documentNumber = "El número de documento es obligatorio.";
    else if (!/^[0-9]+$/.test(docNum)) e.documentNumber = "El número de documento debe contener solo dígitos.";
    else if (documentType === "DNI" && docNum.length !== 8) e.documentNumber = "El DNI debe tener exactamente 8 dígitos.";
    else if (documentType === "RUC" && docNum.length !== 11) e.documentNumber = "El RUC debe tener exactamente 11 dígitos.";
    else if (documentType === "CE" && docNum.length !== 9) e.documentNumber = "El CE debe tener exactamente 9 dígitos.";

    const phones = phoneString.split(",").map((p) => p.trim()).filter(Boolean);
    phones.forEach((p, idx) => {
      if (!/^[0-9]{9}$/.test(p)) e[`phone_${idx}`] = `El teléfono "${p}" debe tener exactamente 9 dígitos y solo números.`;
    });

    if (email.trim()) {
      if (email.trim().length < 10) e.email = "El correo debe tener al menos 10 caracteres.";
      else if (!email.includes("@")) e.email = 'El correo debe contener "@".';
    }

    farmPlots.forEach((fp, idx) => {
      if (!fp.farm_name.trim()) e[`fp_name_${idx}`] = "El nombre del fundo es obligatorio.";
      else if (fp.farm_name.trim().length < 3) e[`fp_name_${idx}`] = "El nombre del fundo debe tener al menos 3 caracteres.";
      else if (!ONLY_LETTERS.test(fp.farm_name.trim())) e[`fp_name_${idx}`] = "El nombre del fundo debe contener solo letras.";
      if (!fp.hectareas || fp.hectareas <= 0) e[`fp_ha_${idx}`] = "Las hectáreas deben ser mayores a 0.";
      if (!fp.type_crop.trim()) e[`fp_crop_${idx}`] = "El tipo de cultivo es obligatorio.";
      else if (fp.type_crop.trim().length <= 10) e[`fp_crop_${idx}`] = "El tipo de cultivo debe tener más de 10 caracteres.";
    });

    if (!department) e.department = "Debe seleccionar un departamento.";
    if (!province) e.province = "Debe seleccionar una provincia.";
    if (!district.trim()) e.district = "Debe ingresar el distrito.";
    if (!settlementType) e.settlementType = "Debe seleccionar el tipo de asentamiento.";
    if (!settlementName.trim() || settlementName.trim().length < 5) e.settlementName = "El nombre del asentamiento debe tener al menos 5 caracteres.";
    if (!streetType) e.streetType = "Debe seleccionar el tipo de vía.";
    if (!streetName.trim() || streetName.trim().length < 5) e.streetName = "El nombre de la vía debe tener al menos 5 caracteres.";
    if (!reference.trim() || reference.trim().length < 10) e.reference = "La referencia debe tener al menos 10 caracteres.";

    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async (ev: React.FormEvent) => {
    ev.preventDefault();
    if (!validate()) return;
    const phones = phoneString.split(",").map((p) => p.trim()).filter(Boolean);
    const payload: Partial<Customer> = {
      first_name: firstName, last_name: lastName,
      document_type: documentType, document_number: documentNumber,
      phone: phones, email,
      registration_date: registrationDate,
      status: initialData ? (initialData.status ?? true) : true,
      farm_plot: farmPlots.length > 0 ? farmPlots.map((fp) => ({ ...fp, hectareas: Number(fp.hectareas) || 0 })) : undefined,
      address: {
        department, province, district,
        especifics: { settlement_type: settlementType, settlement_name: settlementName, street_type: streetType, street_name: streetName, reference },
      },
    };
    await onSubmit(payload);
  };

  const ec = (f: string) => (errors[f] ? "form-input input-error" : "form-input");

  return (
    <form className="customer-form" onSubmit={handleSubmit} noValidate>
      <div className="form-grid-2">
        <div className="form-group">
          <label className="form-label">Nombres *</label>
          <input type="text" className={ec("firstName")} placeholder="Ej. Carlos Eduardo" value={firstName}
            onChange={(e) => { setFirstName(e.target.value); clearError("firstName"); }} />
          {errors.firstName && <span className="field-error">{errors.firstName}</span>}
        </div>
        <div className="form-group">
          <label className="form-label">Apellidos *</label>
          <input type="text" className={ec("lastName")} placeholder="Ej. Mendoza Valdez" value={lastName}
            onChange={(e) => { setLastName(e.target.value); clearError("lastName"); }} />
          {errors.lastName && <span className="field-error">{errors.lastName}</span>}
        </div>
      </div>

      <div className="form-grid-2">
        <div className="form-group">
          <label className="form-label">Tipo Documento *</label>
          <select className="form-select" value={documentType}
            onChange={(e) => { setDocumentType(e.target.value); setDocumentNumber(""); clearError("documentNumber"); }}>
            <option value="DNI">DNI (8 dígitos)</option>
            <option value="RUC">RUC (11 dígitos)</option>
            <option value="CE">Carné Extranjería - CE (9 dígitos)</option>
          </select>
        </div>
        <div className="form-group">
          <label className="form-label">Número Documento *</label>
          <input type="text" className={ec("documentNumber")} placeholder={documentType === "DNI" ? "45891234" : documentType === "RUC" ? "20512345678" : "123456789"}
            value={documentNumber} pattern={getDocNumberPattern()}
            onChange={(e) => { setDocumentNumber(e.target.value); clearError("documentNumber"); }} />
          {errors.documentNumber && <span className="field-error">{errors.documentNumber}</span>}
        </div>
      </div>

      <div className="form-grid-2">
        <div className="form-group">
          <label className="form-label">Teléfono(s) *<span className="form-hint"> (9 dígitos, separados por coma)</span></label>
          <input type="text" className={errors.phone_0 ? "form-input input-error" : "form-input"} placeholder="987654321, 912345678"
            value={phoneString} onChange={(e) => { setPhoneString(e.target.value); clearError("phone_0"); }} />
          {Object.keys(errors).filter(k => k.startsWith("phone_")).map(k => (
            <span key={k} className="field-error">{errors[k]}</span>
          ))}
        </div>
        <div className="form-group">
          <label className="form-label">Correo Electrónico</label>
          <input type="text" className={ec("email")} placeholder="cliente@agrotec.com" value={email}
            onChange={(e) => { setEmail(e.target.value); clearError("email"); }} />
          {errors.email && <span className="field-error">{errors.email}</span>}
        </div>
      </div>

      <div className="form-group" style={{ display: "none" }}>
        <label className="form-label">Fecha de Registro</label>
        <input type="date" className="form-input" value={registrationDate} readOnly />
      </div>

      <div className="form-subcard">
        <h4 className="subcard-title">Dirección de Ubicación / Fundo</h4>

        <div className="form-grid-3" style={{ marginTop: "8px" }}>
          <div className="form-group">
            <label className="form-label">Departamento *</label>
            <select className={errors.department ? "form-select input-error" : "form-select"} value={department}
              onChange={(e) => { setDepartment(e.target.value); clearError("department"); }}>
              {DEPARTMENTS.map(d => <option key={d} value={d}>{d}</option>)}
            </select>
            {errors.department && <span className="field-error">{errors.department}</span>}
          </div>
          <div className="form-group">
            <label className="form-label">Provincia *</label>
            <select className={errors.province ? "form-select input-error" : "form-select"} value={province}
              onChange={(e) => { setProvince(e.target.value); setDistrict(""); clearError("province"); }}>
              <option value="">-- Seleccionar --</option>
              {provinces.map(p => <option key={p} value={p}>{p}</option>)}
            </select>
            {errors.province && <span className="field-error">{errors.province}</span>}
          </div>
          <div className="form-group">
            <label className="form-label">Distrito *</label>
            <input type="text" className={ec("district")} placeholder="Ej. Subtanjalla" value={district}
              onChange={(e) => { setDistrict(e.target.value); clearError("district"); }} />
            {errors.district && <span className="field-error">{errors.district}</span>}
          </div>
        </div>

        <div className="form-grid-3" style={{ marginTop: "8px" }}>
          <div className="form-group">
            <label className="form-label">Tipo Asentamiento *</label>
            <select className={errors.settlementType ? "form-select input-error" : "form-select"} value={settlementType}
              onChange={(e) => { setSettlementType(e.target.value); clearError("settlementType"); }}>
              {SETTLEMENT_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
            </select>
            {errors.settlementType && <span className="field-error">{errors.settlementType}</span>}
          </div>
          <div className="form-group">
            <label className="form-label">Nombre Asentamiento *</label>
            <input type="text" className={ec("settlementName")} placeholder="El Sol (min 5 chars)"
              value={settlementName} onChange={(e) => { setSettlementName(e.target.value); clearError("settlementName"); }} />
            {errors.settlementName && <span className="field-error">{errors.settlementName}</span>}
          </div>
          <div className="form-group">
            <label className="form-label">Tipo de Vía *</label>
            <select className={errors.streetType ? "form-select input-error" : "form-select"} value={streetType}
              onChange={(e) => { setStreetType(e.target.value); clearError("streetType"); }}>
              {STREET_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
            </select>
            {errors.streetType && <span className="field-error">{errors.streetType}</span>}
          </div>
        </div>

        <div className="form-grid-2" style={{ marginTop: "8px" }}>
          <div className="form-group">
            <label className="form-label">Nombre de Vía / Km *</label>
            <input type="text" className={ec("streetName")} placeholder="Panamericana Sur Km 300 (min 5)"
              value={streetName} onChange={(e) => { setStreetName(e.target.value); clearError("streetName"); }} />
            {errors.streetName && <span className="field-error">{errors.streetName}</span>}
          </div>
          <div className="form-group">
            <label className="form-label">Referencia de Llegada *</label>
            <input type="text" className={ec("reference")} placeholder="Frente a grifo Primax (min 10)"
              value={reference} onChange={(e) => { setReference(e.target.value); clearError("reference"); }} />
            {errors.reference && <span className="field-error">{errors.reference}</span>}
          </div>
        </div>
      </div>

      <div className="form-subcard">
        <div className="subcard-header">
          <div>
            <h4 className="subcard-title">Parcelas y Cultivos Asignados</h4>
            <span className="subcard-subtitle">Cultivos del cliente registrados en MongoDB</span>
          </div>
          <Button type="button" variant="outline" size="sm" icon={<Plus size={14} />} onClick={addFarmPlot}>
            Añadir Parcela
          </Button>
        </div>
        <div className="subcard-body">
          {farmPlots.map((plot, idx) => (
            <div key={idx} className="ingredient-row-extended">
              <div className="form-group" style={{ flex: "1 1 140px", margin: 0 }}>
                <input type="text" className={errors[`fp_name_${idx}`] ? "form-input input-error" : "form-input"}
                  placeholder="Nombre Fundo/Parcela" value={plot.farm_name}
                  onChange={(e) => { updateFarmPlot(idx, "farm_name", e.target.value); clearError(`fp_name_${idx}`); }} />
                {errors[`fp_name_${idx}`] && <span className="field-error">{errors[`fp_name_${idx}`]}</span>}
              </div>
              <div className="form-group" style={{ flex: "0 0 100px", margin: 0 }}>
                <input type="number" step="0.1" min="0.1"
                  className={errors[`fp_ha_${idx}`] ? "form-input input-error" : "form-input"}
                  placeholder="Hectáreas" value={plot.hectareas}
                  onChange={(e) => { updateFarmPlot(idx, "hectareas", Number(e.target.value)); clearError(`fp_ha_${idx}`); }} />
                {errors[`fp_ha_${idx}`] && <span className="field-error">{errors[`fp_ha_${idx}`]}</span>}
              </div>
              <div className="form-group" style={{ flex: "1 1 160px", margin: 0 }}>
                <input type="text" className={errors[`fp_crop_${idx}`] ? "form-input input-error" : "form-input"}
                  placeholder="Tipo de Cultivo (min 11 chars)" value={plot.type_crop}
                  onChange={(e) => { updateFarmPlot(idx, "type_crop", e.target.value); clearError(`fp_crop_${idx}`); }} />
                {errors[`fp_crop_${idx}`] && <span className="field-error">{errors[`fp_crop_${idx}`]}</span>}
              </div>
              <button type="button" className="btn-icon-danger" onClick={() => removeFarmPlot(idx)} title="Eliminar parcela"
                style={{ alignSelf: "flex-start", marginTop: "2px" }}>
                <Trash2 size={16} />
              </button>
            </div>
          ))}
          {farmPlots.length === 0 && <p className="empty-hint">No hay parcelas asignadas.</p>}
        </div>
      </div>

      <div className="form-actions">
        <Button type="button" variant="outline" onClick={onCancel}>Cancelar</Button>
        <Button type="submit" variant="primary" isLoading={isSaving}>
          {initialData ? "Actualizar Cliente" : "Registrar Cliente"}
        </Button>
      </div>
    </form>
  );
};



