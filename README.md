# Backend de B&R

Java 22, Maven, Spring Boot y H2 en archivo. La API pública está bajo `/api/properties` y las imágenes bajo `/api/images`.

## Desarrollo local

Desde esta carpeta, en PowerShell:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-22'
$env:BYR_ADMIN_USERNAME = 'admin'
$env:BYR_ADMIN_PASSWORD = [System.Net.NetworkCredential]::new('', (Read-Host 'Contraseña inicial: al menos 12 caracteres' -AsSecureString)).Password
mvn.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

El perfil `local` usa `./local-data/byr` y `./local-data/images`, atiende en `http://localhost:8080` y admite el frontend en `http://localhost:3000`. Esos datos están ignorados por Git. No copiar la base publicada encima de la local sin un respaldo.

La cuenta inicial se crea **solo si `admin_users` está vacía**. Reiniciar con otra contraseña en las variables no reemplaza una cuenta existente. Se puede retirar `BYR_ADMIN_PASSWORD` del entorno después de crear el administrador. No hay contraseña predeterminada ni registro público.

## Autenticación

- Usuarios en `admin_users`, contraseñas con BCrypt (coste 12).
- `GET /api/auth/csrf`: token CSRF para enviar como `X-CSRF-TOKEN`.
- `POST /api/auth/login`: formulario `username` / `password`; inicia la sesión y cambia su identificador.
- `GET /api/auth/me`: usuario de la sesión actual, o 401.
- `POST /api/auth/logout`: invalida la sesión.
- `POST /api/auth/password`: JSON con `currentPassword` y `newPassword`; exige la contraseña actual y cierra todas las sesiones del usuario.
- Cinco intentos de login fallidos bloquean la cuenta durante 15 minutos. Se persiste en la base.
- Contraseñas nuevas: mínimo 12 caracteres y máximo 72 bytes UTF-8 (límite de BCrypt).

La cookie de sesión es HttpOnly, SameSite=Lax y Secure en producción; el perfil local permite HTTP. La sesión vence a los 30 minutos de inactividad. El cliente debe renovar el token CSRF después de iniciar o cerrar sesión; no se reintentan escrituras automáticamente.

No se implementó recuperación por correo ni administración de múltiples usuarios desde la interfaz. El cambio de contraseña exige conocer la actual. No reutilizar las antiguas credenciales del frontend, que podían estar expuestas en el código publicado.

## Despliegue

1. Respaldar la base H2 y el directorio real de imágenes antes de actualizar. La ubicación existente de la base en `application.properties` se conservó, incluyendo la modificación local previa.
2. Configurar `BYR_ADMIN_USERNAME` y `BYR_ADMIN_PASSWORD` en el servidor para el primer arranque.
3. Revisar `file.storage.location`: debe apuntar al almacenamiento real existente. Su valor previo es `/otp/fileStorage`; no cambiarlo a `/opt` por suposición.
4. `BYR_PUBLIC_BASE_URL` controla la URL pública de imágenes y `BYR_ALLOWED_ORIGINS` acepta una lista de orígenes separados por comas. Ambos usan `https://inmobiliariabyr.com.ar` por defecto.
5. Ejecutar `mvn.cmd clean verify` (o `mvn clean verify` en Linux). El JAR está en `target/ByR-0.0.1-SNAPSHOT.jar`.
6. Publicar frontend y backend coordinadamente: el frontend viejo no puede realizar operaciones administrativas con esta API protegida. El proxy debe conservar cookies y cabeceras CSRF, servir HTTPS y reenviar `/api/*` al backend. El límite de upload del proxy también debe permitir las solicitudes de hasta 30 MB.
7. Verificar login, creación/edición, orden de imágenes, cambio de contraseña y logout en un entorno de prueba antes de publicar.

El esquema continúa gestionado mediante `ddl-auto=update`; se añade `admin_users`. La consola web de H2 queda desactivada. Las sesiones y su registro están en memoria, pensados para una instancia del backend: un reinicio cierra las sesiones. Para varias instancias hace falta un repositorio de sesiones compartido.

## Pruebas y almacenamiento

`mvn.cmd test` usa una base H2 **en memoria** y `target/test-images`, configuradas en `src/test/resources/application.properties`. No usa la base publicada.

Las lecturas construyen copias para la respuesta y no cambian las rutas de imágenes en entidades JPA. Las nuevas fotos se guardan con nombres UUID en directorios independientes del nombre visible de la propiedad. Las rutas antiguas continúan siendo válidas si están dentro de `file.storage.location`.

Los archivos de una carga fallida se limpian al revertir la transacción. En eliminaciones, los registros se confirman antes de borrar archivos. Si el disco falla durante la limpieza, se registra el error y puede quedar un archivo huérfano para limpieza operativa; no existe una transacción atómica entre H2 y el filesystem.

Referencias del diseño: [Spring Security: form login](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/form.html), [CSRF y clientes JavaScript](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html).
