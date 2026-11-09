# template-java-api

Java API for template components

## Dépendances privées (GitHub Packages)

Le projet consomme la bibliothèque [`fr.tiogars:core`](https://github.com/tiogars/core-java) publiée sur le registre Maven GitHub Packages (`https://maven.pkg.github.com/tiogars/core-java`, dépôt déclaré avec l'id `github` dans le `pom.xml`).

Le registre exige une authentification, même pour un paquet public :

- **En local** : déclarer un serveur `github` dans `~/.m2/settings.xml` qui lit le jeton depuis la variable d'environnement `GITHUB_TOKEN`. Le jeton doit posséder le scope `read:packages` (par exemple `gh auth refresh -s read:packages` puis `$env:GITHUB_TOKEN = gh auth token` avant `mvn`).
- **En CI** : les workflows passent `GITHUB_TOKEN` aux étapes Maven; `actions/setup-java` génère automatiquement le `settings.xml` correspondant (server id `github`).

