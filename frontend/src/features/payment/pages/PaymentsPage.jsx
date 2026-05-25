import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../../shared/components/Navbar';
import Sidebar from '../../shared/components/Sidebar';
import { getMyDeliveries } from '../../tracking/services/trackingApi';
import apiClient from '../../shared/utils/apiClient';

const PaymentsPage = () => {
    const navigate = useNavigate();
    const [payments, setPayments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [user, setUser] = useState(null);
    const [filter, setFilter] = useState('ALL'); // ALL, PAID, PENDING

    useEffect(() => {
        const userData = localStorage.getItem('user');
        if (userData) setUser(JSON.parse(userData));
        fetchPayments();
    }, []);

    const fetchPayments = async () => {
        try {
            const response = await getMyDeliveries();
            // Filter deliveries that have payment info
            const deliveriesWithPayment = response.data.filter(d => d.paymentMethod);
            // Sort by date (newest first)
            deliveriesWithPayment.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
            setPayments(deliveriesWithPayment);
        } catch (err) {
            console.error('Failed to fetch payments:', err);
        } finally {
            setLoading(false);
        }
    };

    const getPaymentStatusBadge = (status) => {
        switch (status) {
            case 'PAID': return 'bg-green-100 text-green-800';
            case 'PENDING': return 'bg-yellow-100 text-yellow-800';
            case 'UNPAID': return 'bg-red-100 text-red-800';
            default: return 'bg-gray-100 text-gray-800';
        }
    };

    const getPaymentMethodIcon = (method) => {
        switch (method) {
            case 'COD': return '💵';
            case 'STRIPE': return '💳';
            case 'PAYMONGO_GCASH': return '📱';
            default: return '💰';
        }
    };

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(amount);
    };

    const formatDate = (dateString) => {
        return new Date(dateString).toLocaleDateString('en-PH', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const handleViewReceipt = (delivery) => {
        // Open a printable receipt modal or navigate to receipt page
        navigate(`/receipt/${delivery.id}`);
    };

    const filteredPayments = payments.filter(p => {
        if (filter === 'ALL') return true;
        return p.paymentStatus === filter;
    });

    const totalPaid = payments
        .filter(p => p.paymentStatus === 'PAID')
        .reduce((sum, p) => sum + (p.estimatedCost || 0), 0);

    if (loading) return (
        <div className="min-h-screen bg-gray-50">
            <Navbar />
            <div className="flex">
                <Sidebar userType={user?.userType || 'SENDER'} />
                <div className="flex-1 p-8 flex justify-center items-center">
                    <div className="text-center">
                        <div className="w-16 h-16 border-4 border-[#2563EB] border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
                        <p>Loading payment history...</p>
                    </div>
                </div>
            </div>
        </div>
    );

    return (
        <div className="min-h-screen bg-gray-50">
            <Navbar />
            <div className="flex">
                <Sidebar userType={user?.userType || 'SENDER'} />
                <div className="flex-1 p-8">
                    {/* Header */}
                    <div className="flex justify-between items-center mb-6">
                        <div>
                            <h1 className="text-2xl font-bold">Payment History</h1>
                            <p className="text-gray-500 mt-1">Track all your transactions and receipts</p>
                        </div>
                        <button
                            onClick={() => navigate(-1)}
                            className="text-[#2563EB] hover:underline"
                        >
                            ← Back
                        </button>
                    </div>

                    {/* Summary Cards */}
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
                        <div className="bg-white rounded-xl shadow-md p-6">
                            <p className="text-gray-500 text-sm">Total Spent</p>
                            <p className="text-3xl font-bold text-[#2563EB]">{formatCurrency(totalPaid)}</p>
                            <p className="text-green-600 text-sm mt-2">Across {payments.filter(p => p.paymentStatus === 'PAID').length} transactions</p>
                        </div>
                        <div className="bg-white rounded-xl shadow-md p-6">
                            <p className="text-gray-500 text-sm">Pending Payments</p>
                            <p className="text-3xl font-bold text-yellow-600">{payments.filter(p => p.paymentStatus === 'PENDING' || p.paymentStatus === 'UNPAID').length}</p>
                            <p className="text-gray-500 text-sm mt-2">Awaiting confirmation</p>
                        </div>
                        <div className="bg-white rounded-xl shadow-md p-6">
                            <p className="text-gray-500 text-sm">Total Transactions</p>
                            <p className="text-3xl font-bold text-gray-800">{payments.length}</p>
                            <p className="text-gray-500 text-sm mt-2">All time</p>
                        </div>
                    </div>

                    {/* Filter Tabs */}
                    <div className="flex gap-2 mb-6 border-b">
                        <button
                            onClick={() => setFilter('ALL')}
                            className={`px-4 py-2 font-medium transition-colors ${filter === 'ALL' ? 'text-[#2563EB] border-b-2 border-[#2563EB]' : 'text-gray-500 hover:text-gray-700'}`}
                        >
                            All Transactions
                        </button>
                        <button
                            onClick={() => setFilter('PAID')}
                            className={`px-4 py-2 font-medium transition-colors ${filter === 'PAID' ? 'text-[#2563EB] border-b-2 border-[#2563EB]' : 'text-gray-500 hover:text-gray-700'}`}
                        >
                            Paid
                        </button>
                        <button
                            onClick={() => setFilter('PENDING')}
                            className={`px-4 py-2 font-medium transition-colors ${filter === 'PENDING' ? 'text-[#2563EB] border-b-2 border-[#2563EB]' : 'text-gray-500 hover:text-gray-700'}`}
                        >
                            Pending
                        </button>
                    </div>

                    {/* Payments List */}
                    {filteredPayments.length === 0 ? (
                        <div className="bg-white rounded-xl shadow-md p-12 text-center text-gray-500">
                            <div className="text-5xl mb-4">💳</div>
                            <p className="text-lg">No payment records found.</p>
                            <p className="text-sm mt-2">Create a delivery to see payment history here.</p>
                            <button
                                onClick={() => navigate('/create-delivery')}
                                className="mt-4 bg-[#2563EB] text-white px-6 py-2 rounded-lg hover:bg-blue-700"
                            >
                                Create Delivery
                            </button>
                        </div>
                    ) : (
                        <div className="space-y-4">
                            {filteredPayments.map((payment) => (
                                <div key={payment.id} className="bg-white rounded-xl shadow-md hover:shadow-lg transition-shadow overflow-hidden">
                                    <div className="p-6">
                                        <div className="flex justify-between items-start flex-wrap gap-4">
                                            <div className="flex-1">
                                                <div className="flex items-center gap-3 mb-2">
                                                    <span className="text-2xl">{getPaymentMethodIcon(payment.paymentMethod)}</span>
                                                    <div>
                                                        <p className="font-semibold text-lg">{payment.trackingNumber}</p>
                                                        <p className="text-sm text-gray-500">
                                                            {formatDate(payment.createdAt)}
                                                        </p>
                                                    </div>
                                                </div>
                                                <div className="grid grid-cols-1 md:grid-cols-2 gap-2 mt-3 text-sm">
                                                    <div>
                                                        <span className="text-gray-500">Payment Method:</span>{' '}
                                                        <span className="font-medium">{payment.paymentMethod}</span>
                                                    </div>
                                                    <div>
                                                        <span className="text-gray-500">Transaction ID:</span>{' '}
                                                        <span className="font-mono text-xs">{payment.id}</span>
                                                    </div>
                                                </div>
                                            </div>
                                            <div className="text-right">
                                                <p className="text-2xl font-bold text-[#2563EB]">{formatCurrency(payment.estimatedCost)}</p>
                                                <div className="mt-2">
                                                    <span className={`px-3 py-1 rounded-full text-xs font-semibold ${getPaymentStatusBadge(payment.paymentStatus)}`}>
                                                        {payment.paymentStatus || 'PENDING'}
                                                    </span>
                                                </div>
                                            </div>
                                        </div>

                                        <div className="border-t mt-4 pt-4 flex justify-between items-center flex-wrap gap-2">
                                            <button
                                                onClick={() => navigate(`/tracking/${payment.id}`)}
                                                className="text-[#2563EB] hover:underline text-sm flex items-center gap-1"
                                            >
                                                📦 View Delivery
                                            </button>
                                            <button
                                                onClick={() => handleViewReceipt(payment)}
                                                className="text-gray-600 hover:text-[#2563EB] text-sm flex items-center gap-1"
                                            >
                                                🧾 Download Receipt
                                            </button>
                                            {payment.paymentStatus !== 'PAID' && payment.paymentMethod === 'STRIPE' && (
                                                <button
                                                    onClick={() => navigate(`/tracking/${payment.id}`)}
                                                    className="bg-yellow-500 text-white px-3 py-1 rounded text-sm hover:bg-yellow-600"
                                                >
                                                    Complete Payment
                                                </button>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default PaymentsPage;