package com.business.trycoreevmtrackerapi;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
        "com.business.trycoreevmtrackerapi.application.service",
        "com.business.trycoreevmtrackerapi.infrastructure.adapter.in.rest.controller"
})
class TrycoreEvmTrackerApiApplicationTests {
}
