import { http } from '../../../shared/utils/http';
import { FieldMeeting } from '../types/meeting.types';

function cleanLocalDateTime(dt?: string): string {
  if (!dt) return '';
  let clean = dt.replace('Z', '').split('.')[0];
  if (clean.length === 16) {
    clean = clean + ':00';
  }
  return clean;
}

export const meetingsService = {
  async getMeetings(): Promise<FieldMeeting[]> {
    return await http.get<FieldMeeting[]>('/field-meetings');
  },

  async getMeetingsByStatus(status: string): Promise<FieldMeeting[]> {
    if (!status || status === 'Todos') return this.getMeetings();
    return await http.get<FieldMeeting[]>('/field-meetings/status/' + status);
  },

  async getMeetingById(id: string): Promise<FieldMeeting> {
    return await http.get<FieldMeeting>('/field-meetings/' + id);
  },

  async nextMeetingId(): Promise<string> {
    try {
      const list = await this.getMeetings();
      const nums = list
        .map((m) => m.id || m._id || '')
        .filter((id) => /^MET\d+$/i.test(id))
        .map((id) => parseInt(id.replace(/\D/g, ''), 10))
        .filter((n) => !isNaN(n));
      const max = nums.length > 0 ? Math.max(...nums) : 0;
      return 'MET' + String(max + 1).padStart(3, '0');
    } catch {
      return 'MET001';
    }
  },

  async createMeeting(meeting: Partial<FieldMeeting>): Promise<FieldMeeting> {
    const payload: Partial<FieldMeeting> = { ...meeting };
    if (!payload.id && !payload._id) {
      const nextId = await this.nextMeetingId();
      payload.id = nextId;
      payload._id = nextId;
    } else {
      if (!payload.id && payload._id) payload.id = payload._id;
      if (!payload._id && payload.id) payload._id = payload.id;
    }
    if (payload.meeting_date_time) {
      payload.meeting_date_time = cleanLocalDateTime(payload.meeting_date_time);
    }
    return await http.post<FieldMeeting>('/field-meetings', payload);
  },

  async updateMeeting(id: string, meeting: Partial<FieldMeeting>): Promise<FieldMeeting> {
    const payload: Record<string, unknown> = { ...meeting };
    // El backend requiere obligatoriamente que el body contenga el id
    payload.id = id;
    payload._id = id;
    if (typeof payload.meeting_date_time === 'string') {
      payload.meeting_date_time = cleanLocalDateTime(payload.meeting_date_time);
    }
    // Remover campos de auditoría para evitar problemas de formato en Jackson
    delete payload.created_at;
    delete payload.update_at;
    delete payload.deleted_at;
    delete payload.restored_at;
    return await http.put<FieldMeeting>('/field-meetings/' + id, payload);
  },

  async deleteMeeting(id: string): Promise<FieldMeeting> {
    return await http.patch<FieldMeeting>('/field-meetings/delete/' + id);
  },

  async restoreMeeting(id: string): Promise<FieldMeeting> {
    return await http.patch<FieldMeeting>('/field-meetings/restore/' + id);
  },
};
