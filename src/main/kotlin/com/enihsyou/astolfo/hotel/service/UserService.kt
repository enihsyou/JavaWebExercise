package com.enihsyou.astolfo.hotel.service

import com.enihsyou.astolfo.hotel.domain.Guest
import com.enihsyou.astolfo.hotel.domain.Transaction
import com.enihsyou.astolfo.hotel.domain.User
import com.enihsyou.astolfo.hotel.exception.注册时用户已存在
import com.enihsyou.astolfo.hotel.exception.用户不存在
import com.enihsyou.astolfo.hotel.exception.用户名和密码不匹配
import com.enihsyou.astolfo.hotel.exception.相同身份证已存在
import com.enihsyou.astolfo.hotel.repository.GuestRepository
import com.enihsyou.astolfo.hotel.repository.TransactionRepository
import com.enihsyou.astolfo.hotel.repository.UserRepository
import com.google.common.hash.Hashing
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets


interface UserService {
    /**
     * 根据想要的类型和信息创建用户
     * @return 创建的用户
     * */
    fun make(
        phoneNumber: String,
        password: String,
        nickname: String = "",
        role: User.UserRole
    ): User

    /**
     * 根据手机号和密码验证并登录
     * 如果错误直接由异常处理跳出
     * */
    fun login(
        phoneNumber: String,
        password: String
    ): User

    /**根据手机号搜索用户,没找到直接抛出错误*/
    fun findByPhone(phone: String): User

    fun updateInformation(
        phone: String,
        user: User
    ): User

    fun findTransactions(
        phone: String,
        pageable: Pageable
    ): List<Transaction>

    fun findGuests(
        phone: String,
        pageable: Pageable
    ): List<Guest>

    fun addGuest(
        phone: String,
        guest: Guest
    )
}

@Service(value = "用户层逻辑")
class UserServiceImpl : UserService {

    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var guestRepository: GuestRepository
    @Autowired lateinit var transactionRepository: TransactionRepository

    override fun findByPhone(phone: String): User
        = existUser(phone)

    override fun updateInformation(phone: String, user: User): User {
        val old_user = findByPhone(phone)
        if (user.password.isNotEmpty())
            old_user.password = getCheckedPassword(user.password)
        if (user.nickname.isNotEmpty())
            old_user.nickname = user.nickname
        userRepository.save(old_user)
        return old_user
    }

    private fun existUser(phone: String): User =
        userRepository.findByPhoneNumber(phone)?.
            let { return it }
            ?: throw throw 用户不存在(phone)

    override fun findTransactions(
        phone: String,
        pageable: Pageable
    ): List<Transaction>
        = existUser(phone).let {
        transactionRepository.findByUser(it, pageable)
    }


    override fun addGuest(
        phone: String,
        guest: Guest
    ) {
        val iden = guest.identification
        existUser(phone).let {
            if (guestRepository.findByIdentification(guest.identification) == null)
                guest.run {
                    guestRepository.save(Guest(identification = iden, name = name, user = it))
                }
            else
                throw 相同身份证已存在(iden)
        }
    }

    override fun findGuests(
        phone: String,
        pageable: Pageable
    ): List<Guest> {
        existUser(phone).let {
            return guestRepository.findByUserId(it.id, pageable)
        }
    }

    private fun getCheckedPassword(password: String): String {
        return if (password.length != 64)
            Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString()
        else
            password
    }

//    fun updateInformation(phone: String,
//                          old_password: String,
//                          new_password: String?,
//                          nickname: String?): User {
//        existUser(phone).let {
//            if (it.password == getCheckedPassword(old_password))
//                new_password?.let { p -> it.password = getCheckedPassword(p) }
//            else throw 用户名和密码不匹配()
//            if (!nickname.isNullOrBlank())
//                nickname?.let { n -> it.nickname = nickname }
//            return it
//        }
//    }

    override fun make(
        phoneNumber: String,
        password: String,
        nickname: String,
        role: User.UserRole
    ): User {
        /*注册并返回*/
        if (userRepository.findByPhoneNumber(phoneNumber) != null)
            throw 注册时用户已存在(phoneNumber)
        val user = User(
            phoneNumber = phoneNumber,
            nickname = nickname,
            /*如果密码不是经过前端哈希的，这里进行哈希*/
            password = getCheckedPassword(password),
            role = role
        )
        userRepository.save(user)
        return user
    }

    override fun login(
        phoneNumber: String,
        password: String
    ): User {
        val user = existUser(phoneNumber)
        if (user.password == getCheckedPassword(password)) {
            return user
        }
        throw 用户名和密码不匹配()
    }
//    {
//        val user = userRepository.findOne(phoneNumber) ?: throw 用户不存在(phoneNumber)
//        return
//    }
}



