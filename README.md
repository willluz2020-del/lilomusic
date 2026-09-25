# VMB Music — Android

Player MP3 local em Kotlin com reprodução em segundo plano.

## Funcionalidades
- Importa múltiplos arquivos MP3 pelo seletor do Android
- Guarda a biblioteca escolhida entre aberturas do app
- Play / pause / próxima / anterior
- Continua tocando ao minimizar o app ou apagar a tela
- Controles de mídia do Android via MediaSessionService
- Interface dark verde premium
- Splash screen e ícone VMB Music
- Sem permissão ampla de armazenamento: usa ACTION_OPEN_DOCUMENT

## Tecnologia
- Kotlin
- AndroidX
- Jetpack Media3 1.11.1
- ExoPlayer
- MediaSessionService

## Como gerar o APK no Android Studio
1. Instale Android Studio e o Android 17 SDK (API 37).
2. Abra esta pasta como projeto.
3. Aguarde o Gradle Sync.
4. Menu **Build > Build APK(s)**.
5. O APK de debug será criado normalmente em `app/build/outputs/apk/debug/app-debug.apk`.

Para publicar, gere uma chave de assinatura e use **Build > Generate Signed App Bundle / APK**.

## Observação
Este ambiente não possui Android SDK/Gradle configurados, portanto o APK binário não foi compilado aqui. O projeto está estruturado para ser aberto e compilado no Android Studio.
