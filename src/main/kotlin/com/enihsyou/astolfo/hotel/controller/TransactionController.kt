package com.enihsyou.astolfo.hotel.controller

import com.enihsyou.astolfo.hotel.domain.Room
import com.enihsyou.astolfo.hotel.domain.Transaction
import com.enihsyou.astolfo.hotel.service.RoomService
import com.enihsyou.astolfo.hotel.service.TransactionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime


@RestController("Transaction接口控制器")
@RequestMapping("/api/transactions")
class TransactionController(@Autowired val transactionService: TransactionService) {

    @GetMapping("/list")
    fun listRoomByDate(
        @RequestParam("from", required = false) from: LocalDateTime = LocalDateTime.now(),
        @RequestParam("to", required = false) to: LocalDateTime = LocalDateTime.MAX,
        @RequestParam("type", required = false) type: Room.RoomType = Room.RoomType.Any,
        @RequestParam("direction", required = false) direction: Room.RoomDirection = Room.RoomDirection.Any,
        @RequestParam("priceFrom", required = false) priceFrom: Int = 0,
        @RequestParam("priceTo", required = false) priceTo: Int = Int.MAX_VALUE,
        pageable: Pageable) {

        val byDateBetween = transactionService.listTransactionByDateBetween(from, to, pageable)
        val byPriceBetween = transactionService.listTransactionByPriceBetween(priceFrom, priceTo, pageable)
        val byDirection = transactionService.listTransactionByDirection(direction, pageable)
        val byType = transactionService.listTransactionByType(type, pageable)
    }
}
