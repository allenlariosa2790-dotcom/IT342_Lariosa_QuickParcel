import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getDeliveryById } from '../../tracking/services/trackingApi';

const ReceiptPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [delivery, setDelivery] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchDelivery();
    }, [id]);

    const fetchDelivery = async () => {
        try {
            const response = await getDeliveryById(id);
            setDelivery(response.data);
        } catch (err) {
            console.error('Failed to fetch delivery:', err);
        } finally {
            setLoading(false);
        }
    };

    const handlePrint = () => {
        window.print();
    };

    if (loading) return <div className="p-8 text-center">Loading receipt...</div>;
    if (!delivery) return <div className="p-8 text-center">Receipt not found.</div>;

    return (
        <div className="min-h-screen bg-gray-50 p-8">
            <div className="max-w-2xl mx-auto bg-white rounded-xl shadow-md p-8 print:shadow-none">
                {/* Receipt Header */}
                <div className="text-center border-b pb-4 mb-4">
                    <h1 className="text-3xl font-bold text-[#2563EB]">QuickParcel</h1>
                    <p className="text-gray-500">Official Payment Receipt</p>
                </div>

                {/* Receipt Details */}
                <div className="space-y-3">
                    <div className="flex justify-between">
                        <span className="text-gray-500">Receipt No:</span>
                        <span className="font-mono">{delivery.trackingNumber}</span>
                    </div>
                    <div className="flex justify-between">
                        <span className="text-gray-500">Date:</span>
                        <span>{new Date(delivery.createdAt).toLocaleString()}</span>
                    </div>
                    <div className="flex justify-between">
                        <span className="text-gray-500">Payment Method:</span>
                        <span className="font-medium">{delivery.paymentMethod}</span>
                    </div>
                    <div className="flex justify-between">
                        <span className="text-gray-500">Payment Status:</span>
                        <span className={`font-medium ${delivery.paymentStatus === 'PAID' ? 'text-green-600' : 'text-yellow-600'}`}>
                            {delivery.paymentStatus || 'PENDING'}
                        </span>
                    </div>
                    <div className="border-t pt-3 mt-3">
                        <div className="flex justify-between font-bold text-lg">
                            <span>Total Amount:</span>
                            <span className="text-[#2563EB]">₱{delivery.estimatedCost?.toFixed(2)}</span>
                        </div>
                    </div>
                </div>

                {/* Footer */}
                <div className="text-center text-gray-400 text-sm mt-8 pt-4 border-t">
                    <p>Thank you for choosing QuickParcel!</p>
                    <p>For support, contact: support@quickparcel.com</p>
                </div>

                {/* Action Buttons */}
                <div className="flex gap-4 mt-6 print:hidden">
                    <button
                        onClick={handlePrint}
                        className="flex-1 bg-[#2563EB] text-white py-2 rounded-lg hover:bg-blue-700"
                    >
                        🖨️ Print Receipt
                    </button>
                    <button
                        onClick={() => navigate('/payments')}
                        className="flex-1 bg-gray-300 text-gray-700 py-2 rounded-lg hover:bg-gray-400"
                    >
                        ← Back to Payments
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ReceiptPage;