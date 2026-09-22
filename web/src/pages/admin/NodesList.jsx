import React, { useEffect, useState } from 'react';
import { nodeService } from '../../api/nodeService';
import { geocodingService } from '../../api/geocodingService';
import BatterySlotManager from './BatterySlotManager';
import NodeUpdateForm from './NodeUpdateForm';

export default function NodesList() {
    const [nodes, setNodes] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    // Modal States
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showSlotModal, setShowSlotModal] = useState(false);
    const [selectedNodeId, setSelectedNodeId] = useState(null);
    const [showUpdateForm, setShowUpdateForm] = useState(false);
    const [selectedNodeForUpdate, setSelectedNodeForUpdate] = useState(null);

    // Form state for creating node
    const [formData, setFormData] = useState({
        nodeName: '',
        address: '',
        capacityKWh: '',
        latitude: null,
        longitude: null
    });
    const [isGeocoding, setIsGeocoding] = useState(false);

    useEffect(() => {
        fetchNodes();
    }, []);

    const fetchNodes = async () => {
        try {
            setIsLoading(true);
            setError(null);
            const data = await nodeService.getAll();
            setNodes(data);
        } catch (err) {
            setError(err.message || "Failed to load nodes");
            setNodes([]);
        } finally {
            setIsLoading(false);
        }
    };

    // Auto-geocode when address changes
    const handleAddressChange = async (e) => {
        const address = e.target.value;
        setFormData({ ...formData, address });

        if (address.length > 10) {
            setIsGeocoding(true);
            try {
                const coords = await geocodingService.getAddressCoordinates(address);
                setFormData({
                    ...formData,
                    address,
                    latitude: coords.latitude,
                    longitude: coords.longitude
                });
            } catch (err) {
                console.error('Geocoding error:', err);
            } finally {
                setIsGeocoding(false);
            }
        }
    };

    const handleCreateNode = async (e) => {
        e.preventDefault();
        if (!formData.latitude || !formData.longitude) {
            alert('Please wait for address geocoding to complete');
            return;
        }

        try {
            await nodeService.create({
                nodeName: formData.nodeName,
                latitude: formData.latitude,
                longitude: formData.longitude,
                address: formData.address,
                capacityKWh: parseFloat(formData.capacityKWh)
            });

            setShowCreateModal(false);
            setFormData({ nodeName: '', address: '', capacityKWh: '', latitude: null, longitude: null });
            fetchNodes();
        } catch (err) {
            alert('Failed to create node: ' + err.message);
        }
    };

    const handleDeactivate = async (id) => {
        if (!confirm('Are you sure you want to deactivate this node?')) return;

        try {
            await nodeService.deactivate(id);
            fetchNodes();
        } catch (err) {
            // Extract the custom error message from the backend response
            const errorMessage = err.response?.data?.message || err.message || 'Failed to deactivate node';
            alert(errorMessage);
        }
    };

    // Handler to open update form
    const handleOpenUpdateForm = (node) => {
        setSelectedNodeForUpdate(node);
        setShowUpdateForm(true);
    };

    // Callback when update succeeds
    const handleUpdateSuccess = () => {
        setShowUpdateForm(false);
        setSelectedNodeForUpdate(null);
        fetchNodes(); // Refresh the table
    };

    // --- Battery Slot Management Handlers ---
    const handleOpenSlotManager = (nodeId) => {
        setSelectedNodeId(nodeId);
        setShowSlotModal(true);
    };

    const handleSlotsSaved = () => {
        setShowSlotModal(false);
        setSelectedNodeId(null);
    };

    return (
        <div className="max-w-7xl">
            <div className="mb-8">
                <h1 className="text-xl font-semibold text-white">Microgrid Nodes</h1>
                <div className="text-slate-500 font-mono text-xs mt-1">solara-grid / administration / nodes</div>
            </div>

            {error && (
                <div className="bg-red-500/10 border border-red-500 text-red-500 px-4 py-3 rounded mb-5 text-sm">
                    {error}
                </div>
            )}

            <div className="flex items-center justify-end mb-5">
                <button
                    onClick={() => setShowCreateModal(true)}
                    className="bg-amber-500 hover:bg-amber-400 text-slate-950 font-semibold px-4 py-2 rounded-lg text-sm transition-colors"
                >
                    + Add Node
                </button>
            </div>

            <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden shadow-lg">
                <table className="w-full text-left border-collapse">
                    <thead>
                        <tr>
                            <th className="px-5 py-3.5 border-b border-slate-800 text-slate-500 font-medium text-[11px]">Node Name</th>
                            <th className="px-5 py-3.5 border-b border-slate-800 text-slate-500 font-medium text-[11px]">Location</th>
                            <th className="px-5 py-3.5 border-b border-slate-800 text-slate-500 font-medium text-[11px]">Capacity (kWh)</th>
                            <th className="px-5 py-3.5 border-b border-slate-800 text-slate-500 font-medium text-[11px]">Status</th>
                            <th className="px-5 py-3.5 border-b border-slate-800 text-slate-500 font-medium text-[11px] text-center">Battery Slot Management</th>
                            <th className="px-5 py-3.5 border-b border-slate-800 text-slate-500 font-medium text-[11px] text-right">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {(nodes || []).map((node, i) => (
                            <tr key={i} className="hover:bg-slate-800/50 transition-colors">
                                <td className="px-5 py-3 border-b border-slate-800 text-sm text-slate-200">{node.nodeName}</td>
                                <td className="px-5 py-3 border-b border-slate-800 text-xs text-slate-400">
                                    {node.location?.address || `${node.location?.latitude?.toFixed(4)}, ${node.location?.longitude?.toFixed(4)}`}
                                </td>
                                <td className="px-5 py-3 border-b border-slate-800 font-mono text-xs text-slate-400">{node.capacityKWh}</td>
                                <td className="px-5 py-3 border-b border-slate-800">
                                    {node.isActive
                                        ? <span className="bg-teal-500/10 text-teal-400 font-mono text-[9px] px-2 py-0.5 rounded-full">ACTIVE</span>
                                        : <span className="bg-slate-500/10 text-slate-400 font-mono text-[9px] px-2 py-0.5 rounded-full">INACTIVE</span>
                                    }
                                </td>

                                {/* NEW DEDICATED COLUMN FOR SLOT MANAGEMENT BUTTON */}
                                <td className="px-5 py-3 border-b border-slate-800 text-center">
                                    <button
                                        onClick={() => handleOpenSlotManager(node.id)}
                                        className="px-3 py-1.5 bg-cyan-500/10 border border-cyan-500/30 text-cyan-400 rounded-lg text-[10px] hover:bg-cyan-500/20 transition-colors font-medium"
                                    >
                                        Manage Slots
                                    </button>
                                </td>

                                {/* ACTIONS COLUMN */}
                                <td className="px-5 py-3 border-b border-slate-800 text-right">

                                    {/* EDIT BUTTON */}
                                    <button
                                        onClick={() => handleOpenUpdateForm(node)}
                                        className="px-2 py-1 border border-amber-500/30 text-amber-400 rounded text-[10px] hover:bg-amber-500/10 transition-colors mr-2"
                                    >
                                        Edit
                                    </button>
                                    {/* DEACTIVATE BUTTON */}
                                    <button
                                        onClick={() => handleDeactivate(node.id)}
                                        disabled={!node.isActive}
                                        className="px-2 py-1 border border-red-500/30 text-red-400 rounded text-[10px] hover:bg-red-500/10 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                                    >
                                        Deactivate
                                    </button>
                                </td>
                            </tr>
                        ))}
                        {!(nodes && nodes.length > 0) && !isLoading && (
                            <tr>
                                {/* UPDATED COLSPAN TO ACCOUNT FOR NEW COLUMN */}
                                <td colSpan="6" className="px-5 py-8 text-center text-slate-500 text-sm">
                                    No nodes found in the database.
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
                {isLoading && (
                    <div className="p-8 text-center text-slate-500 text-sm flex items-center justify-center gap-2">
                        <svg className="animate-spin h-4 w-4 text-amber-500" fill="none" viewBox="0 0 24 24">
                            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                        Fetching from server...
                    </div>
                )}
            </div>

            {/* Create Node Modal */}
            {showCreateModal && (
                <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50">
                    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 w-full max-w-md">
                        <h2 className="text-lg font-semibold text-white mb-4">Add New Microgrid Node</h2>
                        <form onSubmit={handleCreateNode} className="space-y-4">
                            <div>
                                <label className="block text-xs font-medium text-slate-400 mb-2">Node Name</label>
                                <input
                                    type="text"
                                    value={formData.nodeName}
                                    onChange={(e) => setFormData({ ...formData, nodeName: e.target.value })}
                                    className="w-full bg-slate-950/50 border border-slate-800 text-white px-4 py-2 rounded-lg text-sm"
                                    required
                                />
                            </div>
                            <div>
                                <label className="block text-xs font-medium text-slate-400 mb-2">Address</label>
                                <input
                                    type="text"
                                    value={formData.address}
                                    onChange={handleAddressChange}
                                    placeholder="Enter address to auto-fill coordinates"
                                    className="w-full bg-slate-950/50 border border-slate-800 text-white px-4 py-2 rounded-lg text-sm"
                                    required
                                />
                                {isGeocoding && <p className="text-xs text-amber-500 mt-1">Geocoding address...</p>}
                            </div>
                            <div>
                                <label className="block text-xs font-medium text-slate-400 mb-2">Capacity (kWh)</label>
                                <input
                                    type="number"
                                    step="0.1"
                                    value={formData.capacityKWh}
                                    onChange={(e) => setFormData({ ...formData, capacityKWh: e.target.value })}
                                    className="w-full bg-slate-950/50 border border-slate-800 text-white px-4 py-2 rounded-lg text-sm"
                                    required
                                />
                            </div>
                            <div className="flex gap-3 pt-4">
                                <button
                                    type="button"
                                    onClick={() => setShowCreateModal(false)}
                                    className="flex-1 px-4 py-2 border border-slate-600 text-slate-400 rounded-lg hover:bg-slate-800"
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    className="flex-1 px-4 py-2 bg-amber-500 hover:bg-amber-400 text-slate-950 font-semibold rounded-lg"
                                >
                                    Create Node
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Battery Slot Manager Modal (Rendered via Separate Component) */}
            {showSlotModal && (
                <BatterySlotManager
                    nodeId={selectedNodeId}
                    onClose={handleSlotsSaved}
                />
            )}

            {/* Update Node Details Form Modal */}
            {showUpdateForm && (
                <NodeUpdateForm
                    node={selectedNodeForUpdate}
                    onClose={() => {
                        setShowUpdateForm(false);
                        setSelectedNodeForUpdate(null);
                    }}
                    onSuccess={handleUpdateSuccess}
                />
            )}
        </div>
    );
}