import React, { useState, useCallback } from 'react';
import Cropper from 'react-easy-crop';
import getCroppedImg from '../utils/cropImage';

const ImageCropper = ({ image, onCropComplete, onClose }) => {
    const [crop, setCrop] = useState({ x: 0, y: 0 });
    const [zoom, setZoom] = useState(1);
    const [croppedAreaPixels, setCroppedAreaPixels] = useState(null);

    const onCropCompleteHandler = useCallback((croppedArea, croppedAreaPx) => {
        setCroppedAreaPixels(croppedAreaPx);
    }, []);

    const handleCrop = async () => {
        try {
            const croppedImage = await getCroppedImg(image, croppedAreaPixels);
            onCropComplete(croppedImage);
        } catch (e) {
            console.error('Cropping failed:', e);
        }
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-xl max-w-2xl w-full max-h-[90vh] overflow-auto">
                <div className="p-4 border-b">
                    <h3 className="text-lg font-bold">Crop Profile Picture</h3>
                    <p className="text-sm text-gray-500">Adjust the image to center your face</p>
                </div>

                <div className="p-4">
                    <div className="relative h-96 bg-gray-100 rounded-lg overflow-hidden">
                        <Cropper
                            image={image}
                            crop={crop}
                            zoom={zoom}
                            aspect={1}
                            onCropChange={setCrop}
                            onZoomChange={setZoom}
                            onCropComplete={onCropCompleteHandler}
                            cropShape="round"
                            showGrid={true}
                        />
                    </div>

                    <div className="mt-4">
                        <label className="block text-sm font-medium text-gray-700 mb-1">Zoom</label>
                        <input
                            type="range"
                            min={1}
                            max={3}
                            step={0.01}
                            value={zoom}
                            onChange={(e) => setZoom(parseFloat(e.target.value))}
                            className="w-full"
                        />
                    </div>
                </div>

                <div className="p-4 border-t flex gap-3 justify-end">
                    <button
                        onClick={onClose}
                        className="px-4 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400"
                    >
                        Cancel
                    </button>
                    <button
                        onClick={handleCrop}
                        className="px-4 py-2 bg-[#2563EB] text-white rounded-lg hover:bg-blue-700"
                    >
                        Apply Crop
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ImageCropper;