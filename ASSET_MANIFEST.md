# BluePatitas Asset Manifest

The app currently compiles without final image assets. These assets are planned for future UI iterations and must not be required by the build until they are added.

| Asset | Planned screen | Description | Aspect ratio | Recommended size | Format | Required |
| --- | --- | --- | --- | --- | --- | --- |
| `logo_bluepatitas.svg` | App shell, demo access, auth | Primary BluePatitas brand mark with paw and IoT identity. | 1:1 | 512 x 512 | SVG | Required before production |
| `welcome_shelter_iot.webp` | Onboarding / welcome | Shelter monitoring scene with animals and IoT devices. | 16:9 | 1920 x 1080 | WebP | Optional for prototype |
| `auth_header.webp` | Login / Register | Header image for authentication screens. | 3:1 | 1440 x 480 | WebP | Optional for prototype |
| `animal_default.webp` | Animals | Default animal avatar when no photo exists. | 1:1 | 512 x 512 | WebP | Required before Animals release |
| `veterinarian_default.webp` | Veterinarians / Profile | Default veterinarian profile image. | 1:1 | 512 x 512 | WebP | Optional |
| `camera_zone_placeholder.webp` | Monitoring | Static placeholder for zone camera preview or PIR prototype. | 16:9 | 1280 x 720 | WebP | Required before Monitoring release |
| `empty_animals.svg` | Animals | Empty state illustration for shelters without registered animals. | 4:3 | 800 x 600 | SVG | Optional |
| `empty_alerts.svg` | Alerts | Empty state illustration for no active alerts. | 4:3 | 800 x 600 | SVG | Optional |
| `device_offline.svg` | Devices / Alerts | Device disconnected icon or illustration. | 1:1 | 512 x 512 | SVG | Required before Devices release |
