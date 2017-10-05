package pink.madis.agptweeter

import java.nio.charset.StandardCharsets.UTF_8
import java.io.IOException


fun isNewRemoteVersion(remoteVersion: Version, local: Fetcher): Boolean {
  val localVersion = local.latestVersion(remoteVersion.coords) ?: return true // no local, so definitely later
  return remoteVersion > localVersion
}

fun check(artifactSource: ArtifactSource, cache: Store, db: Store, tweet: (String)->Unit) {
  println("Checking for new versions of ${artifactSource.coords}")

  val remote = GoogleFetcher()

  val remoteVersion = remote.latestVersion(artifactSource.coords) ?: throw IOException("Did not get a proper remote version")

  // hit cache first, because the most common case is that we're not hitting anything
  if (!isNewRemoteVersion(remoteVersion, StoreFetcher(cache)) && !isNewRemoteVersion(remoteVersion, StoreFetcher(db))) {
    // no hit, return
    return
  }

  tweet("${artifactSource.prettyName} ${remoteVersion.orig} is out!")

  // update cache and db
  cache.write(artifactSource.coords.toKey(), remoteVersion.orig.toByteArray(UTF_8))
  db.write(artifactSource.coords.toKey(), remoteVersion.orig.toByteArray(UTF_8))
}