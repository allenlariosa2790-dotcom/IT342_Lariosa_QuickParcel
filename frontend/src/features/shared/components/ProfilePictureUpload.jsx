import React, { useState, useEffect } from 'react';
import apiClient from '../utils/apiClient';
import ImageCropper from './ImageCropper';

const ProfilePictureUpload = ({ userId, onUpdate }) => {
    const [uploading, setUploading] = useState(false);
    const [pictureUrl, setPictureUrl] = useState(null);
    const [hasPicture, setHasPicture] = useState(false);
    const [showCropper, setShowCropper] = useState(false);
    const [selectedImage, setSelectedImage] = useState(null);

    useEffect(() => {
        fetchProfilePicture();
    }, [userId]);

    const fetchProfilePicture = async () => {
        try {
            const response = await apiClient.get('/upload/profile-picture');
            if (response.data.hasPicture) {
                const url = `http://localhost:8080${response.data.url}?t=${Date.now()}`;
                setPictureUrl(url);
                setHasPicture(true);
            }
        } catch (error) {
            console.error('Failed to fetch profile picture:', error);
        }
    };

    const handleFileSelect = (e) => {
        const file = e.target.files[0];
        if (!file) return;

        if (!file.type.startsWith('image/')) {
            alert('Please select an image file');
            return;
        }

        if (file.size > 5 * 1024 * 1024) {
            alert('File too large. Maximum size is 5MB');
            return;
        }

        const imageUrl = URL.createObjectURL(file);
        setSelectedImage(imageUrl);
        setShowCropper(true);
    };

    const handleCropComplete = async (croppedFile) => {
        setShowCropper(false);
        await uploadProfilePicture(croppedFile);
        if (selectedImage) {
            URL.revokeObjectURL(selectedImage);
        }
    };

    const uploadProfilePicture = async (file) => {
        const formData = new FormData();
        formData.append('file', file);

        setUploading(true);
        try {
            const response = await apiClient.post('/upload/profile-picture', formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });

            const newUrl = `http://localhost:8080${response.data.fileUrl}?t=${Date.now()}`;
            setPictureUrl(newUrl);
            setHasPicture(true);
            onUpdate?.();
            alert('Profile picture updated successfully!');

            // Dispatch event to update navbar
            window.dispatchEvent(new CustomEvent('profilePictureUpdated'));
        } catch (error) {
            console.error('Upload failed:', error);
            alert(error.response?.data?.error || 'Failed to upload profile picture');
        } finally {
            setUploading(false);
        }
    };

    const handleRemovePicture = async () => {
        if (!window.confirm('Are you sure you want to remove your profile picture?')) return;

        setUploading(true);
        try {
            await apiClient.delete('/upload/profile-picture');
            setPictureUrl(null);
            setHasPicture(false);
            onUpdate?.();
            alert('Profile picture removed successfully');

            // Dispatch event to update navbar
            window.dispatchEvent(new CustomEvent('profilePictureUpdated'));
        } catch (error) {
            console.error('Remove failed:', error);
            alert('Failed to remove profile picture');
        } finally {
            setUploading(false);
        }
    };

    const getProfileImageUrl = () => {
        if (pictureUrl) {
            return pictureUrl;
        }
        return null;
    };

    return (
        <div className="flex flex-col items-center">
            <div className="relative group">
                {hasPicture ? (
                    <img
                        src={getProfileImageUrl()}
                        alt="Profile"
                        className="w-32 h-32 rounded-full object-cover border-4 border-[#2563EB]"
                        onError={(e) => {
                            e.target.src = 'https://via.placeholder.com/128?text=User';
                        }}
                    />
                ) : (
                    <div className="w-32 h-32 rounded-full bg-gray-200 flex items-center justify-center border-4 border-[#2563EB]">
                        <span className="text-4xl text-gray-500">👤</span>
                    </div>
                )}

                {/* Upload Button - SVG without line breaks */}
                <label className="absolute bottom-0 right-0 bg-[#2563EB] rounded-full p-2 cursor-pointer hover:bg-blue-700 transition-colors">
                    <input
                        type="file"
                        accept="image/*"
                        onChange={handleFileSelect}
                        disabled={uploading}
                        className="hidden"
                    />
                    <svg className="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                </label>
            </div>

            {hasPicture && (
                <button
                    onClick={handleRemovePicture}
                    disabled={uploading}
                    className="mt-2 text-red-500 text-sm hover:text-red-600 disabled:opacity-50"
                >
                    Remove Photo
                </button>
            )}

            {uploading && (
                <p className="text-sm text-gray-500 mt-2">Uploading...</p>
            )}

            <p className="text-xs text-gray-400 mt-2">Click the camera icon to change photo</p>
            <p className="text-xs text-gray-400">You can crop and center your image</p>

            {showCropper && selectedImage && (
                <ImageCropper
                    image={selectedImage}
                    onCropComplete={handleCropComplete}
                    onClose={() => {
                        setShowCropper(false);
                        if (selectedImage) {
                            URL.revokeObjectURL(selectedImage);
                        }
                    }}
                />
            )}
        </div>
    );
};

export default ProfilePictureUpload;