import React, { useEffect, useState } from 'react';
import Navbar from '../../shared/components/Navbar';
import Sidebar from '../../shared/components/Sidebar';
import apiClient from '../../shared/utils/apiClient';

const Payments = () => {
    const [payments, setPayments] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchPayments();
    }, []);

    const fetchPayments = async () => {
        try {
            const response = await apiClient.get('/payments/my');
            setPayments(response.data.data || []);
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
                <Sidebar userType="SENDER" />
                <div className="flex-1 p-8">
                    <h1 className="text-2xl font-bold mb-6">Payment History</h1>
                    {payments.length === 0 ? (
                        <div className="bg-white rounded-xl shadow-md p-8 text-center text-gray-500">
                            No payment records found.
                        </div>
                    ) : (
                        <div className="space-y-4">
                            {payments.map((payment) => (
                                <div key={payment.id} className="bg-white rounded-xl shadow-md p-4">
                                    <div className="flex justify-between">
                                        <div>
                                            <p className="font-semibold">Delivery #{payment.deliveryId}</p>
                                            <p className="text-sm text-gray-500">
                                                {new Date(payment.createdAt).toLocaleString()}
                                            </p>
                                        </div>
                                        <div className="text-right">
                                            <p className="font-bold text-[#2563EB]">₱{payment.amount?.toFixed(2)}</p>
                                            <p className={`text-sm ${payment.status === 'COMPLETED' ? 'text-green-600' : 'text-yellow-600'}`}>
                                                {payment.status}
                                            </p>
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

export default Payments;