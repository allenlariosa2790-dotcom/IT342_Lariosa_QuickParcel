import React, { useEffect, useState } from 'react';
import Navbar from '../../shared/components/Navbar';
import Sidebar from '../../shared/components/Sidebar';
import { getMyDeliveries } from '../../tracking/services/trackingApi';

const PaymentsPage = () => {
    const [payments, setPayments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [user, setUser] = useState(null);

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
            setPayments(deliveriesWithPayment);
        } catch (err) {
            console.error('Failed to fetch payments:', err);
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <div className="p-8">Loading...</div>;

    return (
        <div className="min-h-screen bg-gray-50">
            <Navbar />
            <div className="flex">
                <Sidebar userType={user?.userType || 'SENDER'} />
                <div className="flex-1 p-8">
                    <h1 className="text-2xl font-bold mb-6">Payment History</h1>
                    {payments.length === 0 ? (
                        <p className="text-gray-500">No payment records found.</p>
                    ) : (
                        <div className="space-y-4">
                            {payments.map((delivery) => (
                                <div key={delivery.id} className="bg-white rounded-xl shadow-md p-4">
                                    <div className="flex justify-between items-center">
                                        <div>
                                            <p className="font-semibold">{delivery.trackingNumber}</p>
                                            <p className="text-sm text-gray-500">
                                                {new Date(delivery.createdAt).toLocaleDateString()}
                                            </p>
                                            <p className="text-sm">
                                                Payment: {delivery.paymentMethod} -
                                                <span className={delivery.paymentStatus === 'PAID' ? 'text-green-600' : 'text-yellow-600'}>
                                                    {delivery.paymentStatus || 'PENDING'}
                                                </span>
                                            </p>
                                        </div>
                                        <div className="text-right">
                                            <p className="font-bold text-[#2563EB]">₱{delivery.estimatedCost?.toFixed(2)}</p>
                                            {delivery.paymentStatus !== 'PAID' && delivery.paymentMethod === 'STRIPE' && (
                                                <button
                                                    onClick={() => window.location.href = `/tracking/${delivery.id}`}
                                                    className="text-sm text-blue-600 hover:underline"
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