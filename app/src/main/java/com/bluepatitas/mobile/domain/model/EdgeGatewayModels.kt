package com.bluepatitas.mobile.domain.model

const val DEFAULT_EDGE_GATEWAY_URL = "http://10.0.2.2:18090"

data class EdgeGatewaySnapshot(
    val simulatorStatus: EdgeSimulatorStatus,
    val dispenserStatus: DispenserStatus,
    val schedule: DispenserSchedule
)

data class EdgeSimulatorStatus(
    val simulationActive: Boolean,
    val latitude: Double?,
    val longitude: Double?
)

data class DispenserStatus(
    val active: Boolean
)

data class DispenserSchedule(
    val active: Boolean,
    val intervalSeconds: Int?
)
