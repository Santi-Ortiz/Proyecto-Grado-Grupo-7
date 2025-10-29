package com.grupo7.tesis.system;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class RecomendacionMateriasSystemTest {

    private static final String SERVER_URL = "http://localhost:4200";
    private static final String REGISTER_URL = "/registro";
    
    private static final String XPATH_RECOMENDACIONES_NAVBAR = "//nav/a[@href='/recomendaciones']";
    private static final String XPATH_RECOMENDACION_TEXTAREA = "//div[contains(@class, 'input-block')]/textarea";
    private static final String XPATH_RECOMENDACION_TIPO_MATERIA = "//div[contains(@class, 'tipo-materia')]/select";
    private static final String XPATH_RECOMENDACION_BUTTON = "//div[contains(@class, 'formulario')]/button";
    private static final String XPATH_RECOMENDACION_RESPUESTA = "//div[contains(@class, 'respuesta-box')]";
    private static final String XPATH_RECOMENDACION_SUGERENCIA = "//div[contains(@class, 'sugerencias-box')]";

    private static final String TEST_PDF_PATH = "src/test/resources/informes/InformeAvance.pdf";

    private static final String PRIMER_NOMBRE = "TestUser";
    private static final String SEGUNDO_NOMBRE = "Playwright";
    private static final String PRIMER_APELLIDO = "Sistema";
    private static final String SEGUNDO_APELLIDO = "Test";
    private static final String CODIGO_ESTUDIANTIL = "99999999";
    private static final String CARRERA = "Ingeniería de Sistemas";
    private static final String ANIO_INGRESO = "2020";
    private static final String CORREO = "test.playwright@javeriana.edu.co";
    private static final String CONTRASENA = "Password123!";

    private static final String CONSULTA_GENERAL = "¿Qué materias me recomiendas para el próximo semestre?";
    private static final String CONSULTA_ENFASIS = "Quiero tomar materias de énfasis en Inteligencia Artificial";
    private static final String CONSULTA_ELECTIVAS = "Dame recomendaciones de electivas complementarias";
    private static final String CONSULTA_COMPLEMENTARIAS = "Dame recomendaciones de materias complementarias";

    private Playwright playwright;
    private Browser browser;
    private BrowserContext browserContext;
    private Page page;
   
    @BeforeEach
    void init() {
        this.playwright = Playwright.create();

        this.browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(false)
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
        realizarRegistro();
        cargarArchivoPDF();

        page.locator(XPATH_RECOMENDACIONES_NAVBAR).click();
        page.locator(XPATH_RECOMENDACION_TEXTAREA).fill(CONSULTA_GENERAL);
        page.locator(XPATH_RECOMENDACION_TIPO_MATERIA).selectOption("Cualquiera");
        page.locator(XPATH_RECOMENDACION_BUTTON).click();
        page.waitForTimeout(25000);

        assertEquals(true, page.locator(XPATH_RECOMENDACION_RESPUESTA).isVisible());
        assertEquals(true, page.locator(XPATH_RECOMENDACION_SUGERENCIA).isVisible());
   
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

    private void realizarRegistro() {
        page.navigate(SERVER_URL + REGISTER_URL);
        page.waitForLoadState();

        page.locator("input[name='primerNombre']").fill(PRIMER_NOMBRE);
        page.locator("input[name='segundoNombre']").fill(SEGUNDO_NOMBRE);
        page.locator("input[name='primerApellido']").fill(PRIMER_APELLIDO);
        page.locator("input[name='segundoApellido']").fill(SEGUNDO_APELLIDO);
        page.locator("input[formControlName='codigo']").fill(CODIGO_ESTUDIANTIL);
        page.locator("select[formControlName='carrera']").selectOption(CARRERA);
        page.locator("input[formControlName='anioIngreso']").fill(ANIO_INGRESO);
        page.locator("input[name='correo']").fill(CORREO);
        page.locator("input[name='clave']").fill(CONTRASENA);
        page.locator("input[formControlName='confirmarContrasenia']").fill(CONTRASENA);

        page.waitForTimeout(500);

        page.locator("button[type='submit']").click();
    }

    private void cargarArchivoPDF() {

        Locator fileInput = page.locator("input[type='file']");

        Path filePath = Paths.get(TEST_PDF_PATH);
        fileInput.setInputFiles(filePath);

        page.waitForTimeout(200);

        page.locator("button.process-btn").click();
        page.waitForTimeout(600);

    }

}
