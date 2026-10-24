import { useState, useEffect, useCallback } from 'react';
import { FieldMeeting } from '../types/meeting.types';
import { meetingsService } from '../services/meetingsService';

export function useMeetings() {
  const [meetings, setMeetings] = useState<FieldMeeting[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [filterStatus, setFilterStatus] = useState<string>('Todos');

  const fetchMeetings = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await meetingsService.getMeetingsByStatus(filterStatus);
      setMeetings(Array.isArray(data) ? data : []);
    } catch (err: unknown) {
      console.error('Error fetching meetings:', err);
      setError(err instanceof Error ? err.message : 'Error al cargar visitas de campo');
    } finally {
      setIsLoading(false);
    }
  }, [filterStatus]);

  useEffect(() => {
    fetchMeetings();
  }, [fetchMeetings]);

  const saveMeeting = async (data: Partial<FieldMeeting>, id?: string) => {
    try {
      if (id) {
        await meetingsService.updateMeeting(id, data);
      } else {
        await meetingsService.createMeeting(data as Omit<FieldMeeting, 'id' | '_id'>);
      }
      await fetchMeetings();
    } catch (err: unknown) {
      throw err;
    }
  };

  const deleteMeeting = async (id: string) => {
    try {
      let existing = meetings.find((m) => (m.id || m._id) === id);
      if (!existing) {
        try {
          existing = await meetingsService.getMeetingById(id);
        } catch {
          // ignore
        }
      }
      if (existing) {
        await meetingsService.updateMeeting(id, {
          ...existing,
          visit_status: 'Cancelada',
        });
      }
      await meetingsService.deleteMeeting(id);
      await fetchMeetings();
    } catch (err: unknown) {
      throw err;
    }
  };

  const restoreMeeting = async (id: string) => {
    try {
      let existing = meetings.find((m) => (m.id || m._id) === id);
      if (!existing) {
        try {
          existing = await meetingsService.getMeetingById(id);
        } catch {
          // ignore
        }
      }
      if (existing) {
        await meetingsService.updateMeeting(id, {
          ...existing,
          visit_status: 'Pendiente',
        });
      }
      await meetingsService.restoreMeeting(id);
      await fetchMeetings();
    } catch (err: unknown) {
      throw err;
    }
  };

  return {
    meetings,
    isLoading,
    error,
    filterStatus,
    setFilterStatus,
    reload: fetchMeetings,
    saveMeeting,
    deleteMeeting,
    restoreMeeting,
  };
}
