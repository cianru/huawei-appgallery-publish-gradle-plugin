package ru.cian.huawei.publish.service.mock

import ru.cian.huawei.publish.service.HuaweiServiceImpl

class MockServerWrapperStub : MockServerWrapper {

    override fun getBaseUrl(): String {
        return HuaweiServiceImpl.DOMAIN_URL
    }

    override fun start() {
        // nothing;
    }

    override fun shutdown() {
        // nothing;
    }
}