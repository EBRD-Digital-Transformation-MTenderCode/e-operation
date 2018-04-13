package com.procurement.operation.model

const val HEADER_NAME_AUTHORIZATION = "Authorization"
const val AUTHORIZATION_PREFIX_BASIC = "Basic "
const val AUTHORIZATION_PREFIX_BEARER = "Bearer "

const val REALM = """realm="yoda""""
const val BEARER_REALM = AUTHORIZATION_PREFIX_BEARER + REALM

const val HEADER_NAME_WWW_AUTHENTICATE = "WWW-Authenticate"
const val HEADER_NAME_OPERATION_ID = "X-OPERATION-ID"

const val HEADER_NAME_TOKEN_TYPE = "tid"
const val ACCESS_TOKEN_TYPE = "ACCESS"
const val REFRESH_TOKEN_TYPE = "REFRESH"
const val CLAIM_NAME_PLATFORM_ID = "idPlatform"
