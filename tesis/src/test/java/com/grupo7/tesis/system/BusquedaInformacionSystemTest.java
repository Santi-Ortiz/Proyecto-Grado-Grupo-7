package com.grupo7.tesis.system;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class BusquedaInformacionSystemTest {

    private static final String LOGIN_URL = "/login";
    private static final String BUSQUEDA_URL = "/busqueda";

    private static final String TEST_EMAIL = "test@javeriana.edu.co";
    private static final String TEST_PASSWORD = "password123";

    private static final String PROMPT = "¿Cuáles son los prerrequisitos de Cálculo Vectorial?";

    private Playwright playwright;
    private Browser browser;
    private BrowserContext browserContext;
    private Page page;
   
    @BeforeEach
    void init() {

        this.playwright = Playwright.create();
        this.browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(true)
                        .setSlowMo(800));
        this.browserContext = browser.newContext();
        this.page = browserContext.newPage();
    }

    @AfterEach
    void end() {
        browser.close();
        playwright.close();
    }


    // Realizar búsqueda de información exitosa
    @Test
    public void testBusquedaInformacionExitosa() {
    }

    // Búsqueda con consulta vacía
    @Test
    public void testBusquedaConsultaVacia() {

    }


}
