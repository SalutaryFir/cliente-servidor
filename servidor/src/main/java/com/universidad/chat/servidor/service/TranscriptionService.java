package com.universidad.chat.servidor.service;

import org.springframework.stereotype.Service;
import org.vosk.Model;
import org.vosk.Recognizer;
import jakarta.annotation.PostConstruct;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;


@Service
public class TranscriptionService {

    private Model model;

    // Este método se ejecuta cuando Spring crea el servicio
    @PostConstruct
    public void loadModel() {
        String modelName = "vosk-model-small-es-0.42";
        
        // Intentar múltiples rutas posibles
        String[] possiblePaths = {
            modelName,                                           // Ruta relativa desde directorio de ejecución
            "servidor/" + modelName,                            // Ruta desde raíz del proyecto
            "../" + modelName,                                  // Un nivel arriba (cuando se ejecuta desde target)
            "../../" + modelName,                               // Dos niveles arriba
            System.getProperty("user.dir") + "/" + modelName,   // Directorio de trabajo actual
            System.getProperty("user.dir") + "/servidor/" + modelName,
            System.getProperty("user.dir") + "/../" + modelName
        };
        
        File modelDir = null;
        for (String path : possiblePaths) {
            File testDir = new File(path);
            if (testDir.exists() && testDir.isDirectory()) {
                modelDir = testDir;
                System.out.println("✅ Modelo Vosk encontrado en: " + testDir.getAbsolutePath());
                break;
            }
        }
        
        if (modelDir == null) {
            System.err.println("!!! ERROR FATAL: No se pudo encontrar el modelo de Vosk.");
            System.err.println("!!! Directorio de trabajo actual: " + System.getProperty("user.dir"));
            System.err.println("!!! Asegúrate de que la carpeta '" + modelName + "' esté en alguna de estas ubicaciones:");
            for (String path : possiblePaths) {
                System.err.println("!!!   - " + new File(path).getAbsolutePath());
            }
            return;
        }
        
        try {
            model = new Model(modelDir.getAbsolutePath());
            System.out.println(">>> Modelo de Vosk cargado exitosamente desde: " + modelDir.getAbsolutePath() + " <<<");
        } catch (IOException e) {
            System.err.println("!!! ERROR FATAL: No se pudo cargar el modelo de Vosk desde: " + modelDir.getAbsolutePath());
            e.printStackTrace();
        }
    }

    /**
     * Realiza la transcripción de datos de audio WAV usando el modelo de Vosk.
     * @param wavAudioData Los bytes del archivo WAV completo.
     * @return El texto transcribo.
     */
    public String transcribeAudio(byte[] wavAudioData) {
        if (model == null) return "[Error: Modelo no cargado]";

        try (Recognizer recognizer = new Recognizer(model, 16000)) {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new ByteArrayInputStream(wavAudioData));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = ais.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            byte[] audioBytes = baos.toByteArray();

            recognizer.acceptWaveForm(audioBytes, audioBytes.length);
            String json = recognizer.getFinalResult();

            System.out.println("JSON RESULTADO VOSK: " + json);
            String text = json.replaceAll(".*\"text\"\\s*:\\s*\"(.*?)\".*", "$1");
            return text.isEmpty() ? "[Transcripción vacía]" : text;
        } catch (Exception e) {
            e.printStackTrace();
            return "[Error durante la transcripción]";
        }
    }


}