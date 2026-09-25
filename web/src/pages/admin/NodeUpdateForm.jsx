import React, { useState, useEffect } from 'react';
import { nodeService } from '../../api/nodeService';

export default function NodeUpdateForm({ node, onClose, onSuccess }) {
  const [formData, setFormData] = useState({
    capacityKWh: '',
  });
  
  const [scheduleInput, setScheduleInput] = useState('');
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  // Populate form when node prop changes
  useEffect(() => {
    if (node) {
      setFormData({
        capacityKWh: node.capacityKWh || '',
      });
      // Convert array to comma-separated string for editing
      setScheduleInput((node.schedule || []).join(', '));
      setError(null);
      setSuccessMessage(null);
    }
  }, [node]);

  const handleSave = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccessMessage(null);
    
    // Validate capacity
    const capacity = parseFloat(formData.capacityKWh);
    if (!capacity || capacity <= 0) {
      setError('Capacity must be a positive number.');
      return;
    }

    // Parse schedule: split by comma, trim whitespace, filter empty strings
    const parsedSchedule = scheduleInput
      .split(',')
      .map(s => s.trim())
      .filter(s => s.length > 0);

    try {
      setIsSaving(true);
      
      // 1. Update capacity via PUT endpoint
      await nodeService.update(node.id, {
        capacityKWh: capacity
      });

      // 2. Update schedule via PATCH endpoint
      await nodeService.updateSchedule(node.id, parsedSchedule);

      setSuccessMessage('Node details updated successfully!');
      
      // Delay closing to show success message
      setTimeout(() => {
        onSuccess();
      }, 1500);
      
    } catch (err) {
      console.error('Update error:', err);
      setError(err.response?.data?.message || err.message || 'Failed to update node');
    } finally {
      setIsSaving(false);
    }
  };

  if (!node) {
    return (
      <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50 p-4">
        <div className="bg-slate-900 border border-red-500/50 rounded-2xl p-6 w-full max-w-md">
          <h2 className="text-lg font-semibold text-white mb-4">Error</h2>
          <p className="text-red-400 text-sm">Node data is not available.</p>
          <button
            onClick={onClose}
            className="mt-4 w-full px-4 py-2 bg-slate-800 text-white rounded-lg hover:bg-slate-700"
          >
            Close
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 w-full max-w-md shadow-2xl">
        
        {/* Modal Header */}
        <div className="mb-6">
          <h2 className="text-lg font-semibold text-white">Update Node Details</h2>
          <p className="text-slate-500 font-mono text-[10px] mt-1">{node.nodeName}</p>
        </div>

        {/* Error Display */}
        {error && (
          <div className="bg-red-500/10 border border-red-500 text-red-400 px-4 py-3 rounded-lg mb-4 text-xs">
            {error}
          </div>
        )}

        {/* Success Display */}
        {successMessage && (
          <div className="bg-teal-500/10 border border-teal-500 text-teal-400 px-4 py-3 rounded-lg mb-4 text-xs">
            {successMessage}
          </div>
        )}

        <form onSubmit={handleSave} className="space-y-5">
          
          {/* Capacity Field */}
          <div>
            <label className="block text-xs font-medium text-slate-400 mb-2">
              Capacity (kWh)
            </label>
            <input
              type="number"
              step="0.1"
              min="0.1"
              value={formData.capacityKWh}
              onChange={(e) => setFormData({ ...formData, capacityKWh: e.target.value })}
              className="w-full bg-slate-950/50 border border-slate-800 text-white px-4 py-2.5 rounded-lg text-sm outline-none focus:border-amber-500/50 transition-colors"
              required
            />
            <p className="text-[10px] text-slate-600 mt-1">Current: {node.capacityKWh} kWh</p>
          </div>

          {/* Schedule Field */}
          <div>
            <label className="block text-xs font-medium text-slate-400 mb-2">
              Operational Schedule
            </label>
            <textarea
              rows={3}
              value={scheduleInput}
              onChange={(e) => setScheduleInput(e.target.value)}
              placeholder="e.g., Monday-Friday: 8AM-6PM, Saturday: 8AM-2PM"
              className="w-full bg-slate-950/50 border border-slate-800 text-white px-4 py-2.5 rounded-lg text-sm outline-none focus:border-amber-500/50 transition-colors resize-none"
            />
            <p className="text-[10px] text-slate-600 mt-1">Separate entries with commas</p>
            
            {/* Current Schedule Preview */}
            {node.schedule && node.schedule.length > 0 && (
              <div className="mt-2 space-y-1">
                <p className="text-[10px] text-slate-500 font-medium">Current Schedule:</p>
                {node.schedule.map((item, i) => (
                  <span key={i} className="inline-block bg-slate-800 text-slate-300 text-[10px] px-2 py-0.5 rounded mr-1 mb-1">
                    {item}
                  </span>
                ))}
              </div>
            )}
          </div>

          {/* Action Buttons */}
          <div className="flex gap-3 pt-4 border-t border-slate-800">
            <button
              type="button"
              onClick={onClose}
              disabled={isSaving}
              className="flex-1 px-4 py-2.5 border border-slate-600 text-slate-400 rounded-lg hover:bg-slate-800 text-sm transition-colors disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSaving}
              className="flex-1 px-4 py-2.5 bg-amber-500 hover:bg-amber-400 text-slate-950 font-semibold rounded-lg text-sm transition-colors disabled:opacity-50 flex items-center justify-center gap-2"
            >
              {isSaving ? (
                <>
                  <svg className="animate-spin h-4 w-4 text-slate-950" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Saving...
                </>
              ) : (
                'Save Changes'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}