package com.example.nexttrain

data class Ticket(
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
    val startDate: String? = null, // tylko dla okresowego
    val endDate: String? = null, // tylko dla okresowego
    val validityDate: String? = null, // tylko dla jednorazowego
    val departureTime: String? = null, // tylko dla jednorazowego
    val trainNumber: String? = null, // tylko dla jednorazowego
    val seatNumber: String? = null, // tylko dla intercity
    val classNumber: Int? = null, // tylko dla intercity
    val trainCarNumber: Int? = null, // tylko dla intercity
    val trainCarType: String? = null, // tylko dla intercity
)