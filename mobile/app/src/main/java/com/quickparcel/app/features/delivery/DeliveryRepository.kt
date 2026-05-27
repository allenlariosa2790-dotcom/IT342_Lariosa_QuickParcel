package com.quickparcel.app.features.delivery

import com.quickparcel.app.shared.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeliveryRepository(
    private val retrofitClient: RetrofitClient
) {
    private val api = retrofitClient.create(DeliveryApiService::class.java)

    suspend fun calculateDistance(
        pickupAddress: String,
        dropoffAddress: String,
        weight: Double
    ): DeliveryModels.DistanceResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.calculateDistance(
                    DeliveryModels.CalculateDistanceRequest(pickupAddress, dropoffAddress, weight)
                )
                if (response.isSuccessful && response.body() != null) {
                    DeliveryModels.DistanceResult.Success(
                        response.body()!!.distance,
                        response.body()!!.estimatedCost
                    )
                } else {
                    DeliveryModels.DistanceResult.Error(response.message() ?: "Failed to calculate distance")
                }
            } catch (e: Exception) {
                DeliveryModels.DistanceResult.Error(e.message ?: "Network error")
            }
        }
    }

    suspend fun createDelivery(request: DeliveryModels.CreateDeliveryRequest): DeliveryModels.CreateDeliveryResult {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.createDelivery(request)
                if (response.isSuccessful && response.body() != null) {
                    DeliveryModels.CreateDeliveryResult.Success(response.body()!!.data)
                } else {
                    DeliveryModels.CreateDeliveryResult.Error(response.message() ?: "Failed to create delivery")
                }
            } catch (e: Exception) {
                DeliveryModels.CreateDeliveryResult.Error(e.message ?: "Network error")
            }
        }
    }
}