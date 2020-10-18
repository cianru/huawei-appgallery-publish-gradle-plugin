package ru.cian.huawei.publish.models

import org.apache.http.client.HttpResponseException
import ru.cian.huawei.publish.models.response.Ret

class HuaweiHttpResponseException internal constructor(ret: Ret) : HttpResponseException(ret.code, ret.msg)