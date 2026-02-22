package com.example.demo.testutil;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * Meta-annotation combining common test annotations for integration tests
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ActiveProfiles("test")
@Transactional
public @interface IntegrationTest {
}
