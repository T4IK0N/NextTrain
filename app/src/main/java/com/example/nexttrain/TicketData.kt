package com.example.nexttrain

data class TicketData(
    val type: String = "", // "PRZEJAZD TAM", "MIESIĘCZNY T/P" lub "MIESIĘCZNY TAM"
    val normalTicket: Int = 0,
    val reducedTicket: Int = 0,
    val route: String = "",
    val owner: String = "",
    val price: Double = 0.0,
    val ticketNumber: String = "",
    val carrier: String = "", //przewoźnik
    val trainType: String = "", //regio or not
    val discountCategory: String = "",
    val discountPercent: Int = 0,
    val travelDistance: Int = 0,
    val startDate: String?, // tylko dla okresowego
    val endDate: String?, // tylko dla okresowego
    val validityDate: String?, // tylko dla jednorazowego
    val departureTime: String?, // tylko dla jednorazowego
    val trainNumber: String? // tylko dla jednorazowego
)

