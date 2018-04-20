package com.procurement.operation.model

const val AUTHORIZATION_HEADER_NAME = "Authorization"
const val AUTHORIZATION_PREFIX_BASIC = "Basic "
const val AUTHORIZATION_PREFIX_BEARER = "Bearer "

const val REALM = """realm="yoda""""
const val BEARER_REALM = AUTHORIZATION_PREFIX_BEARER + REALM
const val WWW_AUTHENTICATE_HEADER_NAME = "WWW-Authenticate"
const val OPERATION_ID_HEADER_NAME = "X-OPERATION-ID"

const val HEADER_NAME_TOKEN_TYPE = "tid"
const val ACCESS_TOKEN_TYPE = "ACCESS"
const val REFRESH_TOKEN_TYPE = "REFRESH"
const val CLAIM_NAME_PLATFORM_ID = "idPlatform"
