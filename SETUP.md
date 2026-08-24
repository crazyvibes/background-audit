# Before you push this

Fifteen minutes of work, in order.

1. **Claim the namespace.** `io.github.crazyvibes` works if that's your GitHub username.
   If not, find-and-replace it across every file — it appears in the package path, the
   `namespace`, the `group`, the API dump and the README.

   ```bash
   grep -rl 'crazyvibes' . | xargs sed -i 's/crazyvibes/YOUR_USERNAME/g'
   # then rename the directories under src/main/kotlin and src/test/kotlin
   ```

2. **Add the Gradle wrapper.** Not committed here.

   ```bash
   gradle wrapper --gradle-version 8.9
   git add gradle/wrapper gradlew gradlew.bat
   ```

3. **Regenerate the API dump.** The committed one is written by hand and will not match
   byte-for-byte. Overwrite it with the real thing, and check the diff — reading your own
   API as a flat list of JVM signatures is the fastest way to spot something that should
   have been internal.

   ```bash
   ./gradlew apiDump
   ./gradlew apiCheck   # must pass before you push
   ```

4. **Run the tests.** `./gradlew testDebugUnitTest`

5. **Replace LICENSE** with the full Apache 2.0 text from apache.org.

6. **First commit and tag.**

   ```bash
   git init && git add -A
   git commit -m "BackgroundAudit 0.1.0 — report why background work is killed on this device"
   git tag v0.1.0
   git push -u origin main --tags
   ```

7. **Repo settings on GitHub.** Description: *"Reports at runtime why Android background
   work is likely to be killed on this device — Doze, App Standby, vendor autostart
   managers."* Topics: `android`, `kotlin`, `background-execution`, `doze`,
   `battery-optimization`, `android-sdk`, `location`. Enable Issues. Enable Discussions.

8. **Publish to Maven Central.** The build is already wired for it
   (`com.vanniktech.maven.publish`, configured in `background-audit/build.gradle.kts`
   under `mavenPublishing { ... }`) and a release workflow exists at
   `.github/workflows/release.yml`, triggered on any `v*` tag push. What's left is account
   setup, which only you can do:

   1. Create an account at [central.sonatype.com](https://central.sonatype.com) — sign in
      with **your `crazyvibes` GitHub account** via OAuth. This automatically verifies the
      `io.github.crazyvibes` namespace (the `group` this project already uses), no DNS
      records needed.
   2. In the portal, go to **Account → Generate User Token**. This gives you a
      `mavenCentralUsername` / `mavenCentralPassword` pair — not your login password, a
      generated token.
   3. Generate a GPG key pair if you don't have one, and publish the public key to a
      keyserver Central checks (e.g. `keys.openpgp.org` or `keyserver.ubuntu.com`):
      ```bash
      gpg --gen-key
      gpg --list-secret-keys --keyid-format long   # note the key ID
      gpg --armor --export-secret-keys <KEY_ID> > private-key.asc
      gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>
      ```
      Keep `private-key.asc` out of the repo. Delete it once it's stored in secrets below.
   4. For **local publishing** (`./gradlew publishAndReleaseToMavenCentral`), put these in
      `~/.gradle/gradle.properties` (never in the repo):
      ```properties
      mavenCentralUsername=<token username>
      mavenCentralPassword=<token password>
      signingInMemoryKey=<contents of private-key.asc>
      signingInMemoryKeyId=<last 8 chars of the key ID>
      signingInMemoryKeyPassword=<your GPG key passphrase>
      ```
   5. For **CI publishing** (the tag-triggered workflow), add the same five values as
      repository secrets under Settings → Secrets and variables → Actions:
      `MAVEN_CENTRAL_USERNAME`, `MAVEN_CENTRAL_PASSWORD`, `SIGNING_IN_MEMORY_KEY`,
      `SIGNING_IN_MEMORY_KEY_ID`, `SIGNING_IN_MEMORY_KEY_PASSWORD`.
   6. Push a `v*` tag (or run `./gradlew publishAndReleaseToMavenCentral` locally) to
      publish. `automaticRelease = true` is set, so a successful publish goes straight to
      Central without a manual "release" click in the portal — the first release on a new
      namespace can still take a few minutes to a few hours to become searchable.

   JitPack keeps working as-is regardless — the two are independent and use different
   coordinates (`com.github.crazyvibes:background-audit:v0.1.1` on JitPack vs.
   `io.github.crazyvibes:background-audit:0.1.1` on Maven Central).

## What to do with it once it's up

Add it to your LinkedIn Featured section and the Projects section of your resume. Link it
in every cold email. Then write the blog post (`week3-blog-post.md`), which is the
distribution channel — the repo is the proof, the post is what gets people to it.
