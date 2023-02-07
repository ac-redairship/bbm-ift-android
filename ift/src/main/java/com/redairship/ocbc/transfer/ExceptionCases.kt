package com.ocbc.transfer

class InsufficientBalanceException() : Exception()

class NoInternetException : Exception("Please check your network connection and try again.")

class UnexpectedException(message: String) : Exception(message)

class NetworkException(cause: Throwable) : Exception(cause)

class SessionExpireException(override var message: String? = null) : Exception()

class TimeoutException(override var message: String? = null) : Exception()
