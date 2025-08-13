package com.example.nexttrain

import android.annotation.SuppressLint
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.LinearLayout
import android.widget.TextView

class TicketManager {
    fun parseTicketPolregio(text: String): Ticket {
        val normalTicket = Regex("""Normalny:\s*(\d+)""").find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val reducedTicket = Regex("""Ulgowy.*?:\s*(\d+)""").find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0

        val owner = Regex("""Właściciel: (.+)""").find(text)?.groupValues?.get(1) ?: ""
        val priceRegex = Regex("""Opłata za\s*\n?\s*przejazd:\s*([\d,.]+)\s*[\u00A0 ]?zł""")
        val price = priceRegex.find(text)?.groupValues?.get(1)
            ?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

        val ticketNumber = Regex("""\b([A-Z]{2}\d{8})\b""").find(text)?.groupValues?.get(1) ?: ""

        val carrierMatch = Regex("""Przewoźnik:\s*(\S+)\s+Rodzaj pociągu:\s*(\S+)""").find(text)
        val carrier = carrierMatch?.groupValues?.get(1) ?: ""
        val trainType = carrierMatch?.groupValues?.get(2) ?: ""

        val discountMatch = Regex("""([A-Z/]+)\s+(\d{1,2})%""").find(text)
        val discountCategory = discountMatch?.groupValues?.get(1) ?: ""
        val discountPercent = discountMatch?.groupValues?.get(2)?.toIntOrNull() ?: 0

        val travelDistance = Regex("""KM:\s*(\d{1,4})""").find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0

        val routes = Regex("""\* \* ([\p{L} ]+?) {2}([\p{L} ]+?) \* \*""").findAll(text).toList()
        val route: String
        var typeForPeriodicIfExist = "ERROR"
        when (routes.size) {
            0 -> route = "Brak"
            1 -> {
                route = "${routes[0].groupValues[1].trim()} → ${routes[0].groupValues[2].trim()}"
                typeForPeriodicIfExist = "MIESIĘCZNY TAM"
            }
            2 -> {
                route = "${routes[0].groupValues[1].trim()} → ${routes[0].groupValues[2].trim()}"
                typeForPeriodicIfExist = "MIESIĘCZNY T/P"
            }
            else -> route = "ERROR"
        }

        // czy okresowy?
        val periodicMatch = Regex("""Ważny od (\d{2}\.\d{2}\.\d{4}) do (\d{2}\.\d{2}\.\d{4})""").find(text)

        return if (periodicMatch != null) {
            Ticket( // okresowy
                type = typeForPeriodicIfExist, //2
                normalTicket = normalTicket, //2
                reducedTicket = reducedTicket, //2
                route = route, //1
                owner = owner, //1
                price = price, //3
                ticketNumber = ticketNumber, //0
                carrier = carrier, //3
                trainType = trainType, //3
                discountCategory = discountCategory, //2 +1
                discountPercent = discountPercent, //2 +2
                travelDistance = travelDistance, //3
                startDate = periodicMatch.groupValues[1], //2
                endDate = periodicMatch.groupValues[2], //2
                validityDate = null,
                departureTime = null,
                trainNumber = null
            )
        } else {
            val validityDate = Regex("""Ważny (\d{2}\.\d{2}\.\d{4})""").find(text)?.groupValues?.get(1)
            val departureTime = Regex("""Bilet ważny 3 godzin od (\d{2}:\d{2})""").find(text)?.groupValues?.get(1)
            val trainNumber = Regex("""Train REGIO No\. (\d+)""").find(text)?.groupValues?.get(1)

            Ticket( // jednorazowy
                type = "PRZEJAZD TAM",
                normalTicket = normalTicket,
                reducedTicket = reducedTicket,
                route = route,
                owner = owner,
                price = price,
                ticketNumber = ticketNumber,
                carrier = carrier,
                trainType = trainType,
                discountCategory = discountCategory,
                discountPercent = discountPercent,
                travelDistance = travelDistance,
                startDate = null,
                endDate = null,
                validityDate = validityDate,
                departureTime = departureTime,
                trainNumber = trainNumber
            )
        }
    }

    fun parseTicketIntercity(text: String): Ticket {
        val ticketNumber = Regex("""BILET NR (\w{2}\s?\d{8})""").find(text)?.groupValues?.get(1)?.replace(" ", "") ?: ""

        val owner = Regex("""PODRÓŻNY\s+([^\n]+)""").find(text)?.groupValues?.get(1)?.trim() ?: ""

        val discountMatch = Regex("""Ulga\s*(\d{1,2})%""").find(text)
        val discountPercent = discountMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val discountCategory = Regex("""1x\s*([A-Z/]+)""").find(text)?.groupValues?.get(1) ?: ""

        val price = Regex("""CENA\s+([\d,]+)\s*zł""").find(text)?.groupValues?.get(1)
            ?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

        val route = Regex("""\n\s*([A-ZŁŻŚĆŃĘÓa-złżśźćńęó .]+?)\s*ODJAZD""").find(text)?.groupValues?.get(1)?.trim()
            ?: "Brak"

        val departureTime = Regex("""ODJAZD\s+(\d{2}:\d{2})""").find(text)?.groupValues?.get(1)
        val validityDate = Regex("""DATA\s+(\d{2}\.\d{2}\.\d{4})""").find(text)?.groupValues?.get(1)

        val trainNumber = Regex("""POCIĄG\s+(\d+)""").find(text)?.groupValues?.get(1)
        val travelDistance = Regex("""ODLEGŁOŚĆ TARYFOWA\s+(\d+)\s*km""").find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0

        val carrier = Regex("""PRZEWOŹNIK/SPRZEDAWCA\s+([^\n]+)""").find(text)?.groupValues?.get(1)?.trim() ?: "PKP INTERCITY"
        val trainType = "INTERCITY"

        val trainCarNumber = Regex("""WAGON\s+(\d{1,2})""").find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val seatNumber = Regex("""MIEJSCA\s+([^\n]+)""").find(text)?.groupValues?.get(1)?.trim() ?: ""

        val classNumber = Regex("""klasa\s+(\d)""").find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0

        val isCompartmentless = Regex("""Wagon bez\s+przedziałów""").containsMatchIn(text)
        val trainCarType = if (isCompartmentless) "BP" else "P"

        return Ticket(
            type = "PRZEJAZD TAM",
            normalTicket = if (discountPercent == 0) 1 else 0,
            reducedTicket = if (discountPercent > 0) 1 else 0,
            route = route,
            owner = owner,
            price = price,
            ticketNumber = ticketNumber,
            carrier = carrier,
            trainType = trainType,
            discountCategory = discountCategory,
            discountPercent = discountPercent,
            travelDistance = travelDistance,
            startDate = null,
            endDate = null,
            validityDate = validityDate,
            departureTime = departureTime,
            trainNumber = trainNumber,
            seatNumber = seatNumber,
            classNumber = classNumber,
            trainCarNumber = trainCarNumber,
            trainCarType = trainCarType,
        )
    }


    @SuppressLint("SetTextI18n")
    fun updateTicketUI(ticket: Ticket, view: View) {
        if (ticket.type == "PRZEJAZD TAM") {
            // mimo ze takie id to trudno sie mowi
            view.findViewById<TextView>(R.id.startDate).text = "Ważny do: ${ticket.validityDate}" //validity date
            view.findViewById<TextView>(R.id.endDate).text = "Data odjazdu: ${ticket.departureTime}" //departure time
            val trainNumber = view.findViewById<TextView>(R.id.trainNumber)
            trainNumber.text = "Numer pociągu: ${ticket.trainNumber}"
            trainNumber.visibility = VISIBLE
        } else {
            view.findViewById<TextView>(R.id.startDate).text = "Ważny od: ${ticket.startDate}" //validity date
            view.findViewById<TextView>(R.id.endDate).text = "Ważny do: ${ticket.endDate}" //departure time
            view.findViewById<TextView>(R.id.trainNumber).visibility = GONE
        }
        if (ticket.carrier == "POLREGIO") {
            view.findViewById<LinearLayout>(R.id.fourthSection).visibility = GONE
            view.findViewById<View>(R.id.fourthSectionDivider).visibility = GONE
//            val imageView = view.findViewById<ImageView>(R.id.qrImageView);
//            imageView.layoutParams.width = 1000
//            imageView.layoutParams.height = 1000
        }
        if (ticket.carrier == "PKP INTERCITY") {
            view.findViewById<LinearLayout>(R.id.fourthSection).visibility = VISIBLE
            view.findViewById<View>(R.id.fourthSectionDivider).visibility = VISIBLE
            view.findViewById<TextView>(R.id.seatNumber).text = "Miejsca: ${ticket.seatNumber}"
            view.findViewById<TextView>(R.id.classNumber).text = "Klasa: ${ticket.classNumber}"
            view.findViewById<TextView>(R.id.trainCarNumber).text = "Wagon: ${ticket.trainCarNumber}"
            view.findViewById<TextView>(R.id.trainCarType).text = "Typ: ${ticket.trainCarType}"
        }
//        Log.e("TICKET", ticket.carrier)
        view.findViewById<TextView>(R.id.ticketNumber).text = ticket.ticketNumber
        view.findViewById<TextView>(R.id.route).text = ticket.route
        view.findViewById<TextView>(R.id.owner).text = "Właściciel: ${ticket.owner}"
        view.findViewById<TextView>(R.id.type).text = ticket.type
        view.findViewById<TextView>(R.id.normalTicket).text = "Normalny: ${ticket.normalTicket}"
        view.findViewById<TextView>(R.id.reducedTicket).text = "Ulgowy (76): ${ticket.reducedTicket}"
        view.findViewById<TextView>(R.id.discount).text = "${ticket.discountCategory} ${ticket.discountPercent}%"
        view.findViewById<TextView>(R.id.carrier).text = "Przewoźnik: ${ticket.carrier}"
        view.findViewById<TextView>(R.id.trainType).text = "Typ pociągu: ${ticket.trainType}"
        view.findViewById<TextView>(R.id.price).text = "Cena: ${ticket.price} PLN"
        view.findViewById<TextView>(R.id.travelDistance).text = "Dystans: ${ticket.travelDistance} KM"
    }
}