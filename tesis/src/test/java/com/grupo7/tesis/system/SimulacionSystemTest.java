package com.grupo7.tesis.system;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class SimulacionSystemTest {
    private static final String SERVER_URL = "http://localhost:4200";
    private static final String REGISTER_URL = "/registro";
    private static final String SIMULACION_URL = "/simulaciones";
    private static final String SIMULACION_RESULTADO_URL = "/simulaciones/mostrar";
    private static final String HISTORIAL_SIMULACION_URL = "/simulaciones/historial";

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

    @Test
    void testHacerSimulacion(){
        realizarRegistro();
        cargarArchivoPDF();

        page.waitForTimeout(3000);
        seleccionarSimulacion();
        page.waitForTimeout(2000);
        String urlActual = page.url();
        assertEquals(SERVER_URL + SIMULACION_URL, urlActual);
        llenarFormularioSimulacion();
        urlActual = page.url();
        assertEquals(SERVER_URL + SIMULACION_RESULTADO_URL, urlActual);
        verSimulacionPensum();
        verResultadoSimulacion();
        guardarSimulacion();
        verHistorialSimulacion();
        urlActual = page.url();
        assertEquals(SERVER_URL + HISTORIAL_SIMULACION_URL, urlActual);
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

    public void seleccionarSimulacion() {
        Locator planLink = page.locator("a:has-text(\"Simulaciones\")");
        planLink.click();
    }

    private void llenarFormularioSimulacion() {
        page.locator("input[name='nombreSimulacion']").fill("playwrightprueba1");
        page.locator("input[name='semestreInput']").fill("4");
        page.locator("select[name='tipoMatricula']").selectOption("Matrícula completa");
        page.locator("input[name='creditosInput']").fill("18");
        page.locator("input[name='materiasInput']").fill("7");
        page.locator("label[for='prioridad-complementarias']").click();
        page.locator("label[for='prioridad-enfasis']").click();
        page.locator("label[for='practicaProfesional']").click();
        page.waitForTimeout(500);
        page.locator("button.simulacion-button").click();
        esperarResultado("playwrightprueba1");
        page.waitForTimeout(2000);
    }

    private void verSimulacionPensum(){
        page.locator("button.visualizar-simulacion").click();
        page.waitForTimeout(3000);
        page.locator("button.volver-simulacion").click();
    }

    private void verResultadoSimulacion(){
        int lastHeight = 0;

        while (true) {
            page.keyboard().press("PageDown");
            page.waitForTimeout(500);
            lastHeight ++;
            if (lastHeight > 7) {
                break;
            }
        }

    }

    private void guardarSimulacion(){
        page.waitForTimeout(3000);
        page.locator("button.guardar-simulacion").click();
        page.waitForTimeout(2000);
        page.locator("button.btn-cerrar").click();
        page.waitForTimeout(2000);
    }

    private void verHistorialSimulacion(){
        page.locator("button.nueva-simulacion").click();
        page.waitForTimeout(3000);
        page.locator("button.historial-button").click();
        page.waitForTimeout(2000);
    }

    
    private void esperarResultado(String nombreSimulacion) {
        String selector = String.format("div.job-item:has-text(\"%s\") button.btn-ver-resultado", nombreSimulacion);
        try {
            page.waitForSelector(selector, new Page.WaitForSelectorOptions().setTimeout(120000));
            page.locator(selector).click();
            page.waitForTimeout(2000);
        } catch (PlaywrightException e) {
            long deadline = System.currentTimeMillis() + 120_000;
            boolean clicked = false;
            while (System.currentTimeMillis() < deadline && !clicked) {
                Locator btn = page.locator(selector);
                if (btn.count() > 0) {
                    try {
                        btn.first().click();
                        clicked = true;
                    } catch (Exception ex) {
                        try {
                            btn.first().evaluate("el => el.click()");
                            clicked = true;
                        } catch (Exception ignore) {
                        }
                    }
                }
                if (!clicked) {
                    page.waitForTimeout(1000);
                }
            }
            if (!clicked) {
                throw new RuntimeException("Timed out waiting for simulation result button for: " + nombreSimulacion, e);
            }
        }
    }

}
