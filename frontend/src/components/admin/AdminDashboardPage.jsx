import { useState, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import ApiService from "../../services/ApiService";
import useError from "../common/ErrorDisplay";

import { Pie, Line } from "react-chartjs-2";
import { Chart, registerables } from "chart.js";

Chart.register(...registerables);

const AdminDashboardPage = () => {
  const { ErrorDisplay, showError } = useError();
  const navigate = useNavigate();

  const [stats, setStats] = useState({
    totalOrders: 0,
    totalRevenue: 0,
    activeCustomers: 0,
    menu: 0,
    recentOrders: [],
    orderStatusDistribution: {},
    revenueData: [],
    popularItems: []
  });

  const fetchDashboardData = useCallback(async () => {
    try {
      const ordersResponse = await ApiService.getAllOrders();
      const menuResponse = await ApiService.getAllMenus();
      const paymentsResponse = await ApiService.getAllPayments();
      const activeCustomerResponse =
        await ApiService.countTotalActiveCustomers();

      // ✅ CORRECT RESPONSE UNWRAPPING
      const orders = ordersResponse?.data?.content || [];
      const menu = menuResponse?.data || [];
      const payments = paymentsResponse?.data || [];
      const activeCustomers = activeCustomerResponse?.data || 0;

      // ===============================
      // CALCULATIONS
      // ===============================

      const totalOrders = orders.length;
      const recentOrders = orders.slice(0, 5);

      const statusCounts = orders.reduce((acc, order) => {
        acc[order.orderStatus] = (acc[order.orderStatus] || 0) + 1;
        return acc;
      }, {});

      const itemCounts = {};
      orders.forEach(order => {
        order.orderItems?.forEach(item => {
          const name = item.menu?.name;
          if (!name) return;
          itemCounts[name] = (itemCounts[name] || 0) + item.quantity;
        });
      });

      const popularItems = Object.entries(itemCounts)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 5);

      const totalRevenue = payments.reduce(
        (sum, p) =>
          p.paymentStatus === "COMPLETED" ? sum + p.amount : sum,
        0
      );

      const revenueByMonth = Array(12).fill(0);
      payments.forEach(p => {
        if (p.paymentStatus === "COMPLETED") {
          const month = new Date(p.paymentDate).getMonth();
          revenueByMonth[month] += p.amount;
        }
      });

      setStats({
        totalOrders,
        totalRevenue,
        activeCustomers,
        menu: menu.length,
        recentOrders,
        orderStatusDistribution: statusCounts,
        revenueData: revenueByMonth,
        popularItems
      });

    } catch (error) {
      showError(error.response?.data?.message || error.message);
    }
  }, [showError]);

  useEffect(() => {
    fetchDashboardData();
  }, [fetchDashboardData]);

  const handleViewOrder = id => {
    navigate(`/admin/orders/${id}`);
  };

  // Chart data
  const revenueChartData = {
    labels: ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'],
    datasets: [{
      label: 'Monthly Revenue ($)',
      data: stats.revenueData,
      backgroundColor: 'rgba(54, 162, 235, 0.2)',
      borderColor: 'rgba(54, 162, 235, 1)',
      borderWidth: 2
    }]
  };

  const statusChartData = {
    labels: Object.keys(stats.orderStatusDistribution),
    datasets: [{
      data: Object.values(stats.orderStatusDistribution),
      backgroundColor: [
        '#ff6384',
        '#36a2eb',
        '#ffce56',
        '#4bc0c0',
        '#9966ff',
        '#ff9f40'
      ]
    }]
  };

  return (
    <div className="admin-dashboard">
      <ErrorDisplay />

      <div className="content-header">
        <h1>Dashboard Overview</h1>
        <button className="refresh-btn" onClick={fetchDashboardData}>
          Refresh Data
        </button>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <h3>Total Orders</h3>
          <p className="stat-value">{stats.totalOrders}</p>
        </div>
        <div className="stat-card">
          <h3>Total Revenue</h3>
          <p className="stat-value">${stats.totalRevenue.toFixed(2)}</p>
        </div>
        <div className="stat-card">
          <h3>Active Customers</h3>
          <p className="stat-value">{stats.activeCustomers}</p>
        </div>
        <div className="stat-card">
          <h3>Menu Items</h3>
          <p className="stat-value">{stats.menu}</p>
        </div>
      </div>

      <div className="charts-row">
        <div className="chart-card">
          <h3>Monthly Revenue</h3>
          <Line data={revenueChartData} />
        </div>

        <div className="chart-card">
          <h3>Order Status Distribution</h3>
          <Pie data={statusChartData} />
        </div>
      </div>

      <div className="data-tables">
        <div className="recent-orders">
          <h3>Recent Orders</h3>
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Date</th>
                <th>Customer</th>
                <th>Amount</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {stats.recentOrders.map(order => (
                <tr key={order.id}>
                  <td>#{order.id}</td>
                  <td>{new Date(order.orderDate).toLocaleDateString()}</td>
                  <td>{order.user?.name || 'N/A'}</td>
                  <td>${order.totalAmount?.toFixed(2)}</td>
                  <td>{order.orderStatus}</td>
                  <td>
                    <button onClick={() => handleViewOrder(order.id)}>
                      View
                    </button>
                  </td>
                </tr>
              ))}
              {stats.recentOrders.length === 0 && (
                <tr>
                  <td colSpan="6">No recent orders</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="popular-items">
          <h3>Most Popular Items</h3>
          <table>
            <thead>
              <tr>
                <th>Item</th>
                <th>Orders</th>
              </tr>
            </thead>
            <tbody>
              {stats.popularItems.map(([name, count]) => (
                <tr key={name}>
                  <td>{name}</td>
                  <td>{count}</td>
                </tr>
              ))}
              {stats.popularItems.length === 0 && (
                <tr>
                  <td colSpan="2">No data</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboardPage;
