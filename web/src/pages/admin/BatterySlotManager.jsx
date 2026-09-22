import React, { useState, useEffect } from 'react';
import { nodeService } from '../../api/nodeService';

export default function BatterySlotManager({ nodeId, onClose }) {
  const [slots, setSlots] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState(null);

  // Fetch existing slots when modal opens
  useEffect(() => {
    const fetchSlots = async () => {
      try {
        setIsLoading(true);
        const data = await nodeService.getBatterySlots(nodeId);
        
        // Format dates for datetime-local input (YYYY-MM-DDTHH:mm)
        const formattedSlots = (data || []).map(slot => {
          // Convert UTC to local time for display
          const startDate = slot.startTime ? new Date(slot.startTime) : null;
          const endDate = slot.endTime ? new Date(slot.endTime) : null;
          
          // Format as YYYY-MM-DDTHH:mm for datetime-local input
          const formatLocalDateTime = (date) => {
            if (!date) return '';
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            const hours = String(date.getHours()).padStart(2, '0');
            const minutes = String(date.getMinutes()).padStart(2, '0');
            return `${year}-${month}-${day}T${hours}:${minutes}`;
          };
          
          return {
            ...slot,
            _id: slot.id || slot._id, 
            startTime: formatLocalDateTime(startDate),
            endTime: formatLocalDateTime(endDate)
          };
        });
        
        setSlots(formattedSlots);
      } catch (err) {
        console.error("Failed to load slots", err);
        setSlots([]);
      } finally {
        setIsLoading(false);
      }
    };

    if (nodeId) fetchSlots();
  }, [nodeId]);

  const addSlot = () => {
    setSlots([...slots, { startTime: '', endTime: '', status: 'Available', _id: null }]);
  };

  const removeSlot = (index) => {
    setSlots(slots.filter((_, i) => i !== index));
  };

  const updateSlot = (index, field, value) => {
    const newSlots = [...slots];
    newSlots[index][field] = value;
    setSlots(newSlots);
  };

  const handleSaveSlots = async () => {
    const validSlots = slots.filter(s => s.startTime && s.endTime);
    
    if (validSlots.length === 0) {
      setError('Please add at least one valid time slot.');
      return;
    }

    const payload = validSlots.map(s => ({
      microGridId: nodeId,
      startTime: new Date(s.startTime).toISOString(),
      endTime: new Date(s.endTime).toISOString(),
      status: s.status
    }));

    try {
      setIsSaving(true);
      setError(null);
      await nodeService.updateBatterySlots(nodeId, payload);
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save slots');
    } finally {
      setIsSaving(false);
    }
  };

  const getStatusClasses = (status) => {
    switch (status) {
      case 'Available':
        return 'border-teal-500/40 text-teal-400 focus:border-teal-500 focus:ring-1 focus:ring-teal-500/20 bg-teal-500/5';
      case 'Booked':
        return 'border-amber-500/40 text-amber-400 focus:border-amber-500 focus:ring-1 focus:ring-amber-500/20 bg-amber-500/5';
      case 'Maintenance':
        return 'border-red-500/40 text-red-400 focus:border-red-500 focus:ring-1 focus:ring-red-500/20 bg-red-500/5';
      default:
        return 'border-slate-700 text-slate-400';
    }
  };

  return (
    <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 w-full max-w-3xl shadow-2xl max-h-[90vh] flex flex-col">
        
        {/* Header */}
        <div className="flex justify-between items-center mb-6 border-b border-slate-800 pb-4">
          <div>
            <h2 className="text-lg font-semibold text-white">Manage Battery Slots</h2>
            <p className="text-slate-500 font-mono text-[10px] mt-1">Node ID: {nodeId}</p>
          </div>
          <button onClick={onClose} className="text-slate-400 hover:text-white text-2xl leading-none">&times;</button>
        </div>

        {/* Error Message */}
        {error && (
          <div className="bg-red-500/10 border border-red-500 text-red-400 px-4 py-2 rounded mb-4 text-xs">
            {error}
          </div>
        )}

        {/* Slots List (Scrollable Area) */}
        <div className="flex-1 overflow-y-auto mb-6 space-y-3 pr-2">
          {isLoading ? (
            <div className="text-center text-slate-500 py-10 text-sm flex items-center justify-center gap-2">
              <svg className="animate-spin h-4 w-4 text-cyan-500" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              Loading slots...
            </div>
          ) : slots.length === 0 ? (
            <div className="text-center text-slate-500 py-10 text-sm border border-dashed border-slate-800 rounded-lg">
              No slots found. Click "Add Time Slot" to create one.
            </div>
          ) : (
            slots.map((slot, index) => (
              <div key={index} className="flex flex-col sm:flex-row gap-3 items-end bg-slate-950/50 p-4 rounded-lg border border-slate-800 hover:border-slate-700 transition-colors">
                
                {/* Start Time */}
                <div className="flex-1 w-full">
                  <label className="block text-[10px] font-medium text-slate-500 mb-1.5">Start Time</label>
                  <input
                    type="datetime-local"
                    value={slot.startTime}
                    onChange={(e) => updateSlot(index, 'startTime', e.target.value)}
                    className="w-full bg-slate-900 border border-slate-700 text-white px-3 py-2 rounded-lg text-xs outline-none focus:border-cyan-500/50"
                  />
                </div>

                {/* End Time */}
                <div className="flex-1 w-full">
                  <label className="block text-[10px] font-medium text-slate-500 mb-1.5">End Time</label>
                  <input
                    type="datetime-local"
                    value={slot.endTime}
                    onChange={(e) => updateSlot(index, 'endTime', e.target.value)}
                    className="w-full bg-slate-900 border border-slate-700 text-white px-3 py-2 rounded-lg text-xs outline-none focus:border-cyan-500/50"
                  />
                </div>

                {/* Color-Coded Status Dropdown */}
                <div className="w-full sm:w-32">
                  <label className="block text-[10px] font-medium text-slate-500 mb-1.5">Status</label>
                  <select
                    value={slot.status}
                    onChange={(e) => updateSlot(index, 'status', e.target.value)}
                    className={`w-full px-3 py-2 rounded-lg text-xs outline-none transition-all ${getStatusClasses(slot.status)}`}
                  >
                    <option value="Available" className="bg-slate-900 text-teal-400">Available</option>
                    <option value="Booked" className="bg-slate-900 text-amber-400">Booked</option>
                    <option value="Maintenance" className="bg-slate-900 text-red-400">Maintenance</option>
                  </select>
                </div>

                {/* Remove Button */}
                <button
                  onClick={() => removeSlot(index)}
                  className="w-full sm:w-auto px-3 py-2 text-red-400 hover:bg-red-500/10 rounded-lg text-xs border border-red-500/30 transition-colors"
                >
                  Remove
                </button>
              </div>
            ))
          )}
        </div>

        {/* Footer Actions */}
        <div className="flex flex-col sm:flex-row gap-3 pt-4 border-t border-slate-800">
          <button
            onClick={addSlot}
            className="sm:flex-1 py-2.5 border border-dashed border-slate-700 text-slate-400 rounded-lg hover:bg-slate-800 hover:text-white text-xs transition-colors flex items-center justify-center gap-2"
          >
            <span className="text-lg leading-none">+</span> Add Time Slot
          </button>
          
          <div className="flex gap-3 sm:w-1/2">
            <button
              onClick={onClose}
              disabled={isSaving}
              className="flex-1 px-4 py-2.5 border border-slate-600 text-slate-400 rounded-lg hover:bg-slate-800 text-sm transition-colors disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              onClick={handleSaveSlots}
              disabled={isSaving || isLoading}
              className="flex-1 px-4 py-2.5 bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-semibold rounded-lg text-sm transition-colors disabled:opacity-50"
            >
              {isSaving ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}