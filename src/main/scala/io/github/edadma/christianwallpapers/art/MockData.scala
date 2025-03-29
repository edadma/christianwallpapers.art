// MockData.scala
package io.github.edadma.christianwallpapers.art

import java.util.Date

// Model classes
case class Wallpaper(
                      id: String,
                      title: String,
                      artist: String,
                      imageUrl: String,
                      dimensions: String,
                      aspectRatio: String,
                      category: String,
                      downloads: Int,
                      dateAdded: Date,
                      featured: Boolean
                    )

case class Artist(
                   id: String,
                   name: String,
                   description: String,
                   avatarUrl: String,
                   totalWallpapers: Int,
                   joinDate: Date
                 )

case class Category(
                     id: String,
                     name: String,
                     description: String,
                     wallpaperCount: Int
                   )

// Mock data provider
object MockData {
  // Current time minus days
  private def daysAgo(days: Int): Date = {
    val now = new Date().getTime
    new Date(now - (days * 24 * 60 * 60 * 1000))
  }

  // Sample artists
  val popularArtists: Seq[Artist] = Seq(
    Artist(
      id = "1",
      name = "GraceArtistry",
      description = "Creating scripture-based art since 2020",
      avatarUrl = "https://placehold.co/150x150?text=GA",
      totalWallpapers = 42,
      joinDate = daysAgo(365)
    ),
    Artist(
      id = "2",
      name = "FaithDesigns",
      description = "Digital artist specializing in mobile designs",
      avatarUrl = "https://placehold.co/150x150?text=FD",
      totalWallpapers = 38,
      joinDate = daysAgo(300)
    ),
    Artist(
      id = "3",
      name = "BlessedCreative",
      description = "Nature-inspired Christian photography",
      avatarUrl = "https://placehold.co/150x150?text=BC",
      totalWallpapers = 27,
      joinDate = daysAgo(250)
    ),
    Artist(
      id = "4",
      name = "DivineCrafts",
      description = "Abstract Christian symbolism artwork",
      avatarUrl = "https://placehold.co/150x150?text=DC",
      totalWallpapers = 19,
      joinDate = daysAgo(180)
    )
  )

  // Sample categories
  val categories: Seq[Category] = Seq(
    Category(id = "1", name = "Scripture", description = "Bible verses and quotes", wallpaperCount = 45),
    Category(id = "2", name = "Cross", description = "Images featuring crosses", wallpaperCount = 32),
    Category(id = "3", name = "Nature", description = "God's creation", wallpaperCount = 38),
    Category(id = "4", name = "Worship", description = "Worship and prayer imagery", wallpaperCount = 27),
    Category(id = "5", name = "Abstract", description = "Abstract Christian art", wallpaperCount = 19),
    Category(id = "6", name = "More", description = "Other categories", wallpaperCount = 23)
  )

  // Sample wallpapers
  val allWallpapers: Seq[Wallpaper] = Seq(
    // Featured wallpapers
    Wallpaper(
      id = "1",
      title = "His Grace Remains",
      artist = "GraceArtistry",
      imageUrl = "https://placehold.co/640x480?text=HisGraceRemains",
      dimensions = "1920x1080",
      aspectRatio = "16:9",
      category = "scripture",
      downloads = 1254,
      dateAdded = daysAgo(45),
      featured = true
    ),
    Wallpaper(
      id = "2",
      title = "The Way",
      artist = "FaithDesigns",
      imageUrl = "https://placehold.co/640x480?text=TheWay",
      dimensions = "750x1334",
      aspectRatio = "9:16",
      category = "cross",
      downloads = 982,
      dateAdded = daysAgo(60),
      featured = true
    ),
    Wallpaper(
      id = "3",
      title = "Psalm 23",
      artist = "BlessedCreative",
      imageUrl = "https://placehold.co/640x480?text=Psalm23",
      dimensions = "1920x1080",
      aspectRatio = "16:9",
      category = "scripture",
      downloads = 1087,
      dateAdded = daysAgo(75),
      featured = true
    ),
    Wallpaper(
      id = "4",
      title = "Hope Eternal",
      artist = "DivineCrafts",
      imageUrl = "https://placehold.co/640x480?text=HopeEternal",
      dimensions = "2880x1920",
      aspectRatio = "3:2",
      category = "abstract",
      downloads = 832,
      dateAdded = daysAgo(90),
      featured = true
    ),

    // New wallpapers
    Wallpaper(
      id = "5",
      title = "Strength in Him",
      artist = "LightBearer",
      imageUrl = "https://placehold.co/640x480?text=StrengthInHim",
      dimensions = "1600x1200",
      aspectRatio = "4:3",
      category = "scripture",
      downloads = 342,
      dateAdded = daysA