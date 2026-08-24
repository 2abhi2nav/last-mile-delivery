import React, { useState, useEffect } from 'react';
import axios from 'axios';

const API_BASE = '/api';

export default function App() {
  const [token, setToken] = useState(localStorage.getItem('token') || '');
  const [role, setRole] = useState(localStorage.getItem('role') || 'CUSTOMER');
  const [username, setUsername] = useState('customer_jane');
  const [password, setPassword] = useState('password');
  const [view, setView] = useState('create'); // 'create', 'orders', 'admin'

  // Order Creation & Preview State
  const [originPincode, setOriginPincode] = useState('110001');
  const [destinationPincode, setDestinationPincode] = useState('560001');
  const [orderType, setOrderType] = useState('STANDARD');
  const [isCod, setIsCod] = useState(false);
  const [weightKg, setWeightKg] = useState('2.5');
  const [lengthCm, setLengthCm] = useState('30');
  const [breadthCm, setBreadthCm] = useState('20');
  const [heightCm, setHeightCm] = useState('15');
  const [preview, setPreview] = useState(null);

  // Orders List & Timeline State
  const [orders, setOrders] = useState([]);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [timeline, setTimeline] = useState([]);
  const [rescheduleDate, setRescheduleDate] = useState('');
  const [rescheduleReason, setRescheduleReason] = useState('');

  // Admin Dashboard State
  const [adminOrders, setAdminOrders] = useState([]);
  const [adminSelectedOrder, setAdminSelectedOrder] = useState(null);
  const [adminTimeline, setAdminTimeline] = useState([]);
  const [overrideStatus, setOverrideStatus] = useState('');
  const [assignAgentId, setAssignAgentId] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [zoneFilter, setZoneFilter] = useState('');

  const getHeaders = () => ({
    headers: { Authorization: `Bearer ${token}` }
  });

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const res = await axios.post(`${API_BASE}/auth/signin`, { username, password });
      setToken(res.data.token);
      setRole(res.data.role);
      localStorage.setItem('token', res.data.token);
      localStorage.setItem('role', res.data.role);
      alert('Logged in successfully as ' + res.data.role);
      if (res.data.role === 'ADMIN') {
        setView('admin');
        fetchAdminOrders(res.data.token);
      } else {
        setView('orders');
        fetchCustomerOrders(res.data.token);
      }
    } catch (err) {
      alert('Login failed: ' + (err.response?.data || err.message));
    }
  };

  const handleLogout = () => {
    setToken('');
    localStorage.clear();
    setView('create');
  };

  const handlePreview = async (e) => {
    e.preventDefault();
    try {
      const res = await axios.post(`${API_BASE}/orders/preview`, {
        originPincode,
        destinationPincode,
        orderType,
        isCod,
        weightKg: parseFloat(weightKg),
        lengthCm: parseFloat(lengthCm),
        breadthCm: parseFloat(breadthCm),
        heightCm: parseFloat(heightCm)
      }, getHeaders());
      setPreview(res.data);
    } catch (err) {
      alert('Preview failed: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleConfirmOrder = async () => {
    try {
      const res = await axios.post(`${API_BASE}/orders/confirm`, {
        originPincode,
        destinationPincode,
        orderType,
        isCod,
        weightKg: parseFloat(weightKg),
        lengthCm: parseFloat(lengthCm),
        breadthCm: parseFloat(breadthCm),
        heightCm: parseFloat(heightCm)
      }, getHeaders());
      alert('Order created successfully! Tracking #: ' + res.data.trackingNumber);
      setPreview(null);
      setView('orders');
      fetchCustomerOrders();
    } catch (err) {
      alert('Order confirmation failed: ' + (err.response?.data?.message || err.message));
    }
  };

  const fetchCustomerOrders = async (authToken = token) => {
    if (!authToken) return;
    try {
      // Fetch timeline or list for customer (mocking fetch via admin endpoint or dedicated customer endpoint if available)
      const res = await axios.get(`${API_BASE}/admin/orders`, { headers: { Authorization: `Bearer ${authToken}` } });
      setOrders(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchAdminOrders = async (authToken = token) => {
    if (!authToken) return;
    try {
      let url = `${API_BASE}/admin/orders?`;
      if (statusFilter) url += `status=${statusFilter}&`;
      if (zoneFilter) url += `zone=${zoneFilter}&`;
      const res = await axios.get(url, { headers: { Authorization: `Bearer ${authToken}` } });
      setAdminOrders(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  const viewOrderTimeline = async (orderId) => {
    try {
      const res = await axios.get(`${API_BASE}/orders/${orderId}/timeline`, getHeaders());
      const orderObj = orders.find(o => o.id === orderId) || adminOrders.find(o => o.id === orderId);
      setSelectedOrder(orderObj);
      setTimeline(res.data);
    } catch (err) {
      alert('Failed to load timeline');
    }
  };

  const handleReschedule = async (orderId) => {
    if (!rescheduleDate) {
      alert('Please select a new delivery date');
      return;
    }
    try {
      await axios.post(`${API_BASE}/orders/${orderId}/reschedule`, {
        newDeliveryDate: new Date(rescheduleDate).toISOString(),
        reason: rescheduleReason || 'Customer requested reschedule'
      }, getHeaders());
      alert('Order rescheduled successfully!');
      setRescheduleDate('');
      setRescheduleReason('');
      viewOrderTimeline(orderId);
    } catch (err) {
      alert('Reschedule failed: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleStatusOverride = async (orderId) => {
    if (!overrideStatus) return;
    try {
      await axios.patch(`${API_BASE}/orders/${orderId}/status/override`, { status: overrideStatus }, getHeaders());
      alert('Status overridden successfully!');
      const res = await axios.get(`${API_BASE}/admin/orders/${orderId}`, getHeaders());
      setAdminSelectedOrder(res.data.order);
      setAdminTimeline(res.data.timeline);
      fetchAdminOrders();
    } catch (err) {
      alert('Failed: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleManualAssign = async (orderId) => {
    if (!assignAgentId) return;
    try {
      await axios.post(`${API_BASE}/agents/assign/manual?orderId=${orderId}&agentId=${assignAgentId}`, {}, getHeaders());
      alert('Agent assigned!');
      const res = await axios.get(`${API_BASE}/admin/orders/${orderId}`, getHeaders());
      setAdminSelectedOrder(res.data.order);
      setAdminTimeline(res.data.timeline);
      fetchAdminOrders();
    } catch (err) {
      alert('Failed: ' + (err.response?.data?.message || err.message));
    }
  };

  useEffect(() => {
    if (token && view === 'admin') {
      fetchAdminOrders();
    } else if (token && view === 'orders') {
      fetchCustomerOrders();
    }
  }, [statusFilter, zoneFilter, view]);

  if (!token) {
    return (
      <div style={{ padding: '30px', fontFamily: 'sans-serif', maxWidth: '400px', margin: 'auto', background: '#fdfdfd', border: '1px solid #ddd', borderRadius: '8px', marginTop: '50px' }}>
        <h2>Last-Mile Delivery Login</h2>
        <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div>
            <label>Username: </label>
            <input type="text" value={username} onChange={e => setUsername(e.target.value)} style={{ width: '100%', padding: '6px' }} />
          </div>
          <div>
            <label>Password: </label>
            <input type="password" value={password} onChange={e => setPassword(e.target.value)} style={{ width: '100%', padding: '6px' }} />
          </div>
          <button type="submit" style={{ padding: '10px', background: '#007bff', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Login</button>
        </form>
        <div style={{ marginTop: '15px', fontSize: '13px', color: '#666' }}>
          <p>Test Accounts:</p>
          <ul>
            <li>Admin: <code>admin</code> / <code>password</code></li>
            <li>Customer: <code>customer_jane</code> / <code>password</code></li>
            <li>Agent: <code>agent_john</code> / <code>password</code></li>
          </ul>
        </div>
      </div>
    );
  }

  return (
    <div style={{ padding: '20px', fontFamily: 'sans-serif', maxWidth: '1200px', margin: 'auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #ddd', paddingBottom: '10px', marginBottom: '20px' }}>
        <h2>Last-Mile Delivery Tracker ({role})</h2>
        <div style={{ display: 'flex', gap: '10px' }}>
          {role !== 'ADMIN' && (
            <>
              <button onClick={() => setView('create')} style={{ background: view === 'create' ? '#007bff' : '#eee', color: view === 'create' ? '#fff' : '#000', padding: '8px 12px', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Create Order</button>
              <button onClick={() => { setView('orders'); fetchCustomerOrders(); }} style={{ background: view === 'orders' ? '#007bff' : '#eee', color: view === 'orders' ? '#fff' : '#000', padding: '8px 12px', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>My Orders & Tracking</button>
            </>
          )}
          {role === 'ADMIN' && (
            <button onClick={() => setView('admin')} style={{ background: view === 'admin' ? '#007bff' : '#eee', color: view === 'admin' ? '#fff' : '#000', padding: '8px 12px', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Admin Dashboard</button>
          )}
          <button onClick={handleLogout} style={{ background: '#dc3545', color: '#fff', padding: '8px 12px', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Logout</button>
        </div>
      </div>

      {/* CREATE ORDER VIEW */}
      {view === 'create' && (
        <div style={{ maxWidth: '600px', margin: 'auto', background: '#f9f9f9', padding: '20px', borderRadius: '8px', border: '1px solid #ddd' }}>
          <h3>Create New Order & Charge Preview</h3>
          <form onSubmit={handlePreview} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            <div style={{ display: 'flex', gap: '10px' }}>
              <div style={{ flex: 1 }}>
                <label>Origin Pincode:</label>
                <input type="text" value={originPincode} onChange={e => setOriginPincode(e.target.value)} style={{ width: '100%', padding: '6px' }} required />
              </div>
              <div style={{ flex: 1 }}>
                <label>Destination Pincode:</label>
                <input type="text" value={destinationPincode} onChange={e => setDestinationPincode(e.target.value)} style={{ width: '100%', padding: '6px' }} required />
              </div>
            </div>
            <div style={{ display: 'flex', gap: '10px' }}>
              <div style={{ flex: 1 }}>
                <label>Order Type:</label>
                <select value={orderType} onChange={e => setOrderType(e.target.value)} style={{ width: '100%', padding: '6px' }}>
                  <option value="STANDARD">STANDARD</option>
                  <option value="EXPRESS">EXPRESS</option>
                </select>
              </div>
              <div style={{ flex: 1, display: 'flex', alignItems: 'center', marginTop: '20px' }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                  <input type="checkbox" checked={isCod} onChange={e => setIsCod(e.target.checked)} /> Cash on Delivery (COD)
                </label>
              </div>
            </div>
            <div style={{ display: 'flex', gap: '10px' }}>
              <div style={{ flex: 1 }}><label>Weight (kg):</label><input type="number" step="0.1" value={weightKg} onChange={e => setWeightKg(e.target.value)} style={{ width: '100%', padding: '6px' }} required /></div>
              <div style={{ flex: 1 }}><label>Length (cm):</label><input type="number" step="0.1" value={lengthCm} onChange={e => setLengthCm(e.target.value)} style={{ width: '100%', padding: '6px' }} required /></div>
              <div style={{ flex: 1 }}><label>Breadth (cm):</label><input type="number" step="0.1" value={breadthCm} onChange={e => setBreadthCm(e.target.value)} style={{ width: '100%', padding: '6px' }} required /></div>
              <div style={{ flex: 1 }}><label>Height (cm):</label><input type="number" step="0.1" value={heightCm} onChange={e => setHeightCm(e.target.value)} style={{ width: '100%', padding: '6px' }} required /></div>
            </div>
            <button type="submit" style={{ padding: '10px', background: '#28a745', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>Calculate Charge Preview</button>
          </form>

          {preview && (
            <div style={{ marginTop: '20px', background: '#fff', padding: '15px', borderRadius: '6px', border: '1px solid #c3e6cb' }}>
              <h4>Charge Preview</h4>
              <p><b>Zones:</b> {preview.originZone} -> {preview.destinationZone}</p>
              <p><b>Billable Weight:</b> {preview.billableWeightKg} kg (Actual: {preview.actualWeightKg} kg | Volumetric: {preview.volumetricWeightKg} kg)</p>
              <p><b>Base Rate:</b> ${preview.baseRate} | <b>Per Kg Rate:</b> ${preview.perKgRate}</p>
              {preview.isCod && <p><b>COD Surcharge:</b> ${preview.codSurcharge}</p>}
              <h3 style={{ color: '#28a745' }}>Total Shipping Cost: ${preview.shippingCost}</h3>
              <button onClick={handleConfirmOrder} style={{ width: '100%', padding: '12px', background: '#007bff', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold', fontSize: '16px' }}>Confirm & Place Order</button>
            </div>
          )}
        </div>
      )}

      {/* CUSTOMER ORDERS & LIVE TRACKING VIEW */}
      {view === 'orders' && (
        <div style={{ display: 'flex', gap: '20px' }}>
          <div style={{ flex: 1 }}>
            <h3>My Orders</h3>
            <table border="1" cellPadding="8" style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ background: '#eee' }}>
                  <th>Tracking #</th>
                  <th>Route</th>
                  <th>Cost</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {orders.map(o => (
                  <tr key={o.id}>
                    <td>{o.trackingNumber}</td>
                    <td>{o.originZone} -> {o.destinationZone}</td>
                    <td>${o.shippingCost}</td>
                    <td><b>{o.currentStatus}</b></td>
                    <td><button onClick={() => viewOrderTimeline(o.id)}>Track / Timeline</button></td>
                  </tr>
                ))}
                {orders.length === 0 && <tr><td colSpan="5" style={{ textAlign: 'center' }}>No orders found.</td></tr>}
              </tbody>
            </table>
          </div>

          {selectedOrder && (
            <div style={{ flex: 1, background: '#f9f9f9', padding: '15px', borderRadius: '8px', border: '1px solid #ddd' }}>
              <h3>Live Tracking: {selectedOrder.trackingNumber}</h3>
              <p><b>Current Status:</b> <span style={{ color: '#007bff', fontWeight: 'bold' }}>{selectedOrder.currentStatus}</span></p>
              <p><b>Route:</b> {selectedOrder.originPincode} -> {selectedOrder.destinationPincode}</p>
              <p><b>Cost:</b> ${selectedOrder.shippingCost}</p>

              <h4>Reschedule Delivery</h4>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginBottom: '15px', background: '#fff', padding: '10px', borderRadius: '4px', border: '1px solid #ddd' }}>
                <label>New Delivery Date & Time:</label>
                <input type="datetime-local" value={rescheduleDate} onChange={e => setRescheduleDate(e.target.value)} />
                <input type="text" placeholder="Reason (e.g. Recipient unavailable)" value={rescheduleReason} onChange={e => setRescheduleReason(e.target.value)} />
                <button onClick={() => handleReschedule(selectedOrder.id)} style={{ background: '#ffc107', border: 'none', padding: '8px', cursor: 'pointer', fontWeight: 'bold' }}>Submit Reschedule Request</button>
              </div>

              <h4>Immutable Audit Timeline</h4>
              <ul style={{ paddingLeft: '20px', fontSize: '14px' }}>
                {timeline.map(t => (
                  <li key={t.id} style={{ marginBottom: '8px' }}>
                    <b>{t.status}</b> at {new Date(t.timestamp).toLocaleString()} <br/>
                    <small>Actor Role: {t.actorRole}</small>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}

      {/* ADMIN DASHBOARD VIEW */}
      {view === 'admin' && (
        <div>
          <h3>Admin Dashboard & Order Management</h3>
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
            <button onClick={() => fetchAdminOrders()}>Refresh</button>
          </div>

          <div style={{ display: 'flex', gap: '20px' }}>
            <div style={{ flex: 2 }}>
              <table border="1" cellPadding="8" style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  <tr style={{ background: '#eee' }}>
                    <th>Tracking #</th>
                    <th>Type</th>
                    <th>Route</th>
                    <th>Cost</th>
                    <th>Status</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {adminOrders.map(o => (
                    <tr key={o.id}>
                      <td>{o.trackingNumber}</td>
                      <td>{o.orderType}</td>
                      <td>{o.originZone} -> {o.destinationZone}</td>
                      <td>${o.shippingCost}</td>
                      <td><b>{o.currentStatus}</b></td>
                      <td>
                        <button onClick={async () => {
                          const res = await axios.get(`${API_BASE}/admin/orders/${o.id}`, getHeaders());
                          setAdminSelectedOrder(res.data.order);
                          setAdminTimeline(res.data.timeline);
                        }}>Manage</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {adminSelectedOrder && (
              <div style={{ flex: 1, background: '#f9f9f9', padding: '15px', borderRadius: '5px', border: '1px solid #ddd' }}>
                <h4>Manage Order: {adminSelectedOrder.trackingNumber}</h4>
                <p><b>Status:</b> {adminSelectedOrder.currentStatus}</p>
                <p><b>Cost:</b> ${adminSelectedOrder.shippingCost}</p>

                <h5>Manual Status Override</h5>
                <div style={{ display: 'flex', gap: '5px', marginBottom: '10px' }}>
                  <select value={overrideStatus} onChange={e => setOverrideStatus(e.target.value)}>
                    <option value="">Select Status</option>
                    <option value="CREATED">CREATED</option>
                    <option value="PICKED_UP">PICKED_UP</option>
                    <option value="IN_TRANSIT">IN_TRANSIT</option>
                    <option value="OUT_FOR_DELIVERY">OUT_FOR_DELIVERY</option>
                    <option value="DELIVERED">DELIVERED</option>
                    <option value="RETURNED">RETURNED</option>
                  </select>
                  <button onClick={() => handleStatusOverride(adminSelectedOrder.id)}>Override</button>
                </div>

                <h5>Assign Agent</h5>
                <div style={{ display: 'flex', gap: '5px', marginBottom: '15px' }}>
                  <input type="number" placeholder="Agent ID" value={assignAgentId} onChange={e => setAssignAgentId(e.target.value)} style={{ width: '80px' }} />
                  <button onClick={() => handleManualAssign(adminSelectedOrder.id)}>Assign</button>
                </div>

                <h5>Timeline</h5>
                <ul style={{ paddingLeft: '15px', fontSize: '13px' }}>
                  {adminTimeline.map(t => (
                    <li key={t.id}><b>{t.status}</b> ({t.actorRole}) - {new Date(t.timestamp).toLocaleString()}</li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
