# AdvancedVillages

Rewrite pluginu Villages dla serwerów Spigot 1.21.4.

## Wymagania

- JDK 17+ do uruchomienia Mavena (kod AdvancedVillages jest kompilowany do bytecode Javy 16)
- Java 21+ do uruchomienia serwera Spigot 1.21.4
- Maven 3.9+
- MetaCore 4.0.2-SNAPSHOT zainstalowany w lokalnym lub skonfigurowanym repozytorium Maven

Opcjonalne integracje: Vault, PlaceholderAPI, WorldEdit i WorldGuard.

## Budowanie

```shell
mvn clean package
```

Gotowy plugin jest kopiowany do `Server/plugins/Villages.jar`.

## Tablista

Uklad, naglowek, stopka, animowane strony, czestotliwosc odswiezania i opcjonalne tekstury glow sa konfigurowane w `plugins/AdvancedVillages/addons/tablist.yml`.

Zmiany mozna zastosowac bez restartu komenda `/village admin reload`.

## API addonów

`AdvancedVillages.getInstance().getApi()` udostepnia teraz menedzer wiosek,
usuwania, ulepszen i animacji oraz `getVillageAt(Location)`. Animacje sa
konfigurowane w `plugins/AdvancedVillages/addons/village-animation.yml`, a
kazda wioska ma osobny przelacznik w GUI ustawien. Opcjonalny addon moze wiec
reagowac na istniejace eventy wioski albo wywolac `getAnimationManager()` bez
duplikowania logiki cyklu zycia.

## Edytor budynków wioski

Edytor wymaga zainstalowanego WorldEdit oraz `settings.use-worldedit: true`.
Operator otwiera wybór poziomu komendą `/village edit`. Po wybraniu levelu
plugin znajduje pustą przestrzeń nad mapą, wkleja schemat i pokazuje jego
granice particle. Zapis wykonuje `/village edit save`, a anulowanie
`/village edit cancel`. Poprzedni schemat trafia do katalogu
`plugins/AdvancedVillages/schematics/backups`.

## Tryb developerski

Tryb developerski udostępnia komendy testowe i pomija sprawdzanie licencji. Można go włączyć w wygenerowanym pliku konfiguracyjnym:

```yaml
settings:
  development-mode: true
```

Alternatywnie podczas lokalnego uruchamiania można dodać parametr JVM `-Dadvancedvillages.dev=true`. Nie należy włączać tego trybu na serwerze produkcyjnym.
