package com.example.demo.testutil;

/**
 * Constants used across test classes
 */
public class TestConstants {

    // User test data
    public static final String TEST_USERNAME = "testuser";
    public static final String TEST_EMAIL = "test@example.com";
    public static final String TEST_PASSWORD = "password123";
    public static final String TEST_FULL_NAME = "Test User";

    // Blog test data
    public static final String TEST_BLOG_TITLE = "Test Blog";
    public static final String TEST_BLOG_CONTENT = "Test blog content";
    public static final String BLOG_STATUS_PUBLISHED = "PUBLISHED";
    public static final String BLOG_STATUS_DRAFT = "DRAFT";

    // Shop test data
    public static final String TEST_SHOP_NAME = "Test Shop";
    public static final String TEST_SHOP_ADDRESS = "123 Test Street";
    public static final String SHOP_STATUS_ACTIVE = "ACTIVE";
    public static final String SHOP_STATUS_INACTIVE = "INACTIVE";
    public static final Double TEST_LATITUDE = 10.8231;
    public static final Double TEST_LONGITUDE = 106.6297;
    public static final Double TEST_RADIUS = 5.0;

    // Category test data
    public static final String TEST_CATEGORY_NAME = "Vietnamese Food";
    public static final String TEST_CATEGORY_DESCRIPTION = "Traditional Vietnamese cuisine";

    // MenuItem test data
    public static final String TEST_MENU_ITEM_NAME = "Pho Bo";
    public static final Double TEST_MENU_ITEM_PRICE = 50000.0;

    // Review test data
    public static final Integer TEST_RATING_EXCELLENT = 5;
    public static final Integer TEST_RATING_GOOD = 4;
    public static final Integer TEST_RATING_POOR = 1;
    public static final String TEST_REVIEW_COMMENT = "Excellent food and service!";

    // Common test data
    public static final Long NON_EXISTENT_ID = 999L;
    public static final int EXPECTED_MIN_RATING = 1;
    public static final int EXPECTED_MAX_RATING = 5;

    private TestConstants() {
        // Private constructor to prevent instantiation
    }
}
