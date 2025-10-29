package com.grupo7.tesis.system;

import org.junit.jupiter.api.Test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class RecomendacionMateriasSystemTest {

    private static final String LOGIN_URL = "/login";
    private static final String RECOMENDACION_URL = "/recomendaciones";

    private static final String TEST_EMAIL = "test@javeriana.edu.co";
    private static final String TEST_PASSWORD = "password123";

    private static final String CONSULTA_RECOMENDACION = "¿Qué materias me recomiendas para el próximo semestre?";
    private static final String CONSULTA_ENFASIS = "Quiero tomar materias de énfasis en Inteligencia Artificial";
    private static final String CONSULTA_ELECTIVAS = "Dame recomendaciones de electivas complementarias";

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

    // Recomendación de materias exitosa
    @Test
    public void testRecomendacionMateriasExitosa() {
        
    }

    // Recomendación de materias de énfasis 
    @Test
    public void testRecomendacionEnfasisEspecifico() {
        
    }
    
    // Recomendación de electivas 
    @Test
    public void testRecomendacionElectivasComplementarias() {
        
    }

     // Recomendación de complementarias
    @Test
    public void testRecomendacionComplementarias() {
        
    }

}
