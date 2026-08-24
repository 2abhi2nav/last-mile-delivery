import React, { useState, useEffect } from 'react';
import axios from 'axios';

const API_BASE = '/api';

export default function AdminDashboard() {
  const [orders, setOrders] = useState([]);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [timeline, setTimeline] = useState([]);
  const [agents, setAgents] = useState([]);

  // Filters
  const [statusFilter, setStatusFilter] = useState('');
  const [zoneFilter, setZoneFilter] = useState('');
  const [agentFilter, setAgentFilter] = useState('');

  // Actions
  const [overrideStatus, setOverrideStatus] = useState('');
  const [assignAgentId, setAssignAgentId] = useState('');
  const [token, setToken] = useState(localStorage.getItem('token') || '');
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('password');

  const getHeaders = () => ({
    headers: { Authorization: `Bearer ${token}` }
  });

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const res = await axios.post(`${API_BASE}/auth/signin`, { username, password });
      setToken(res.data.token);
      localStorage.setItem('token', res.data.token);
      alert('Logged in successfully!');
      fetchData(res.data.token);
    } catch (err) {
      alert('Login failed: ' + (err.response?.data || err.message));
    }
  };

  const fetchData = async (authToken = token) => {
    if (!authToken) return;
    try {
      let url = `${API_BASE}/admin/orders?`;
      if (statusFilter) url += `status=${statusFilter}&`;
      if (zoneFilter) url += `zone=${zoneFilter}&`;
      if (agentFilter) url += `agentId=${agentFilter}&`;

      const resOrders = await axios.get(url, { headers: { Authorization: `Bearer ${authToken}` } });
      setOrders(resOrders.data);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    if (token) {
      fetchData();
    }
  }, [statusFilter, zoneFilter, agentFilter]);

  const viewDetail = async (orderId) => {
    try {
      const res = await axios.get(`${API_BASE}/admin/orders/${orderId}`, getHeaders());
      setSelectedOrder(res.data.order);
      setTimeline(res.data.timeline);
    } catch (err) {
      alert('Failed to load order details');
    }
  };

  const handleStatusOverride = async (orderId) => {
    if (!overrideStatus) return;
    try {
      await axios.patch(`${API_BASE}/orders/${orderId}/status/override`, { status: overrideStatus }, getHeaders());
      alert('Status overridden successfully!');
      viewDetail(orderId);
      fetchData();
    } catch (err) {
      alert('Failed to override status: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleManualAssign = async (orderId) => {
    if (!assignAgentId) return;
    try {
      await axios.post(`${API_BASE}/agents/assign/manual?orderId=${orderId}&agentId=${assignAgentId}`, {}, getHeaders());
      alert('Agent assigned successfully!');
      viewDetail(orderId);
      fetchData();
    } catch (err) {
      alert('Failed to assign agent: ' + (err.response?.data?.message || err.message));
    }
  };

  if (!token) {
    return (
      <div style={{ padding: '20px', fontFamily: 'sans-serif', maxWidth: '400px', margin: 'auto' }}>
        <h2>Admin Login</h2>
        <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          <input type="text" placeholder="Username" value={username} onChange={e => setUsername(e.target.value)} />
          <input type="password" placeholder="Password" value={password} onChange={e => setPassword(e.target.value)} />
          <button type="submit">Login</button>
        </form>
      </div>
    );
  }

  return (
    <div style={{ padding: '20px', fontFamily: 'sans-serif' }}>
      <h1>Admin Dashboard & Order Management</h1>

      {/* Filters */}
      <div style={{ display: 'flex', gap: '15px', marginBottom: '20px', background: '#f5f5f5', padding: '15px', borderRadius: '5px' }}>
        <div>
          <label>Status: </label>
          <select value={statusFilter} onChange={e => setStatusFilter(e.target.value)}>
            <option value="">All</option>
            <option value="CREATED">Created</option>
            <option value="PICKED_UP">Picked Up</option>
            <option value="IN_TRANSIT">In Transit</option>
            <option value="OUT_FOR_DELIVERY">Out For Delivery</option>
            <option value="DELIVERED">Delivered</option>
            <option value="RETURNED">Returned</option>
          </select>
        </div>
        <div>
          <label>Zone: </label>
          <input type="text" placeholder="e.g. ZONE_NORTH" value={zoneFilter} onChange={e => setZoneFilter(e.target.value)} />
        </div>
        <button onClick={() => fetchData()}>Refresh</button>
      </div>

      {/* Orders Table */}
      <div style={{ display: 'flex', gap: '20px' }}>
        <div style={{ flex: 2 }}>
          <h3>Orders List</h3>
          <table border="1" cellPadding="8" style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ background: '#eee' }}>
                <th>Tracking #</th>
                <th>Type</th>
                <th>Origin -> Dest</th>
                <th>Cost ($)</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {orders.map(o => (
                <tr key={o.id}>
                  <td>{o.trackingNumber}</td>
                  <td>{o.orderType} {o.cod ? '(COD)' : ''}</td>
                  <td>{o.originZone} -> {o.destinationZone}</td>
                  <td>${o.shippingCost}</td>
                  <td><b>{o.currentStatus}</b></td>
                  <td>
                    <button onClick={() => viewDetail(o.id)}>View Details</button>
                  </td>
                </tr>
              ))}
              {orders.length === 0 && (
                <tr><td colSpan="6" style={{ textAlign: 'center' }}>No orders found.</td></tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Order Detail & Actions Panel */}
        {selectedOrder && (
          <div style={{ flex: 1, background: '#f9f9f9', padding: '15px', borderRadius: '5px', border: '1px solid #ddd' }}>
            <h3>Order Detail: {selectedOrder.trackingNumber}</h3>
            <p><b>Status:</b> {selectedOrder.currentStatus}</p>
            <p><b>Route:</b> {selectedOrder.originPincode} ({selectedOrder.originZone}) -> {selectedOrder.destinationPincode} ({selectedOrder.destinationZone})</p>
            <p><b>Weight:</b> {selectedOrder.billableWeightKg} kg (Actual: {selectedOrder.weightKg} kg, Vol: {selectedOrder.volumetricWeightKg} kg)</p>
            <p><b>Shipping Cost:</b> ${selectedOrder.shippingCost}</p>
            <p><b>Assigned Agent ID:</b> {selectedOrder.assignedAgent ? selectedOrder.assignedAgent.id : 'None'}</p>

            <h4>Manual Status Override</h4>
            <div style={{ display: 'flex', gap: '5px', marginBottom: '15px' }}>
              <select value={overrideStatus} onChange={e => setOverrideStatus(e.target.value)}>
                <option value="">Select Status</option>
                <option value="CREATED">CREATED</option>
                <option value="PICKED_UP">PICKED_UP</option>
                <option value="IN_TRANSIT">IN_TRANSIT</option>
                <option value="OUT_FOR_DELIVERY">OUT_FOR_DELIVERY</option>
                <option value="DELIVERED">DELIVERED</option>
                <option value="RETURNED">RETURNED</option>
              </select>
              <button onClick={() => handleStatusOverride(selectedOrder.id)}>Override</button>
            </div>

            <h4>Manual Agent Reassignment</h4>
            <div style={{ display: 'flex', gap: '5px', marginBottom: '15px' }}>
              <input type="number" placeholder="Agent ID" value={assignAgentId} onChange={e => setAssignAgentId(e.target.value)} style={{ width: '80px' }} />
              <button onClick={() => handleManualAssign(selectedOrder.id)}>Assign</button>
            </div>

            <h4>Immutable Audit Timeline</h4>
            <ul style={{ paddingLeft: '20px', fontSize: '14px' }}>
              {timeline.map(t => (
                <li key={t.id} style={{ marginBottom: '5px' }}>
                  <b>{t.status}</b> at {new Date(t.timestamp).toLocaleString()} <br/>
                  <small>Actor ID: {t.actorId} ({t.actorRole})</small>
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>
    </div>
  );
}
