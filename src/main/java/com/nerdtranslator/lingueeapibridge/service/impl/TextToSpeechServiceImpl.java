package com.nerdtranslator.lingueeapibridge.service.impl;

import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import com.nerdtranslator.lingueeapibridge.service.TextToSpeechService;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;

@Service
public class TextToSpeechServiceImpl implements TextToSpeechService {
    private static final String PATH_TO_CREDENTIALS = "src/main/resources/TextToSpeechCredentials.json";

    @Override
    public byte[] transformTextToSound(String textToTransfer, String langCode) {
        return getSpeechFromText(textToTransfer, langCode);
    }

    private byte[] getSpeechFromText(String textToTransfer, String langCode) {
        byte[] speechResult = null;
        CredentialsProvider credentialsProvider = () -> {
            try (FileInputStream keyStream = new FileInputStream(PATH_TO_CREDENTIALS)) {
                return ServiceAccountCredentials.fromStream(keyStream);
            }
        };
        try (TextToSpeechClient textToSpeechClient =
                     TextToSpeechClient
                        .create(TextToSpeechSettings
                                .newBuilder()
                                .setCredentialsProvider(credentialsProvider)
                                .build())) {
            SynthesisInput input = SynthesisInput.newBuilder()
                    .setText(textToTransfer)
                    .build();

            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode(langCode)
                    .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                    .build();

            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.MP3)
                    .build();

            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
            ByteString audioRepresentationOfText = response.getAudioContent();
            speechResult = audioRepresentationOfText.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (speechResult == null) {
            throw new RuntimeException("The result of text to voice translation wasn't received");
        }
        return speechResult;
    }

}
