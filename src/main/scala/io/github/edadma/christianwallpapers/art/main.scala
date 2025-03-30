package io.github.edadma.christianwallpapers.art

import io.github.edadma.fluxus._
import org.scalajs.dom
import org.scalajs.dom.window
import com.raquo.airstream.state.Var
import com.raquo.airstream.core.Transaction
import scala.scalajs.js.URIUtils

val aspectRatioSignal = Var("")
val dimensionsSignal  = Var("")
val categorySignal    = Var("")
val artistSignal      = Var("")
val sortBySignal      = Var("popular")

// Function to initialize signals from URL params
def initFromQueryParams(params: Map[String, String]): Unit = {
  Transaction { _ =>
    aspectRatioSignal.set(params.getOrElse("aspect", ""))
    dimensionsSignal.set(params.getOrElse("dimensions", ""))
    categorySignal.set(params.getOrElse("category", ""))
    artistSignal.set(params.getOrElse("artist", ""))
    sortBySignal.set(params.getOrElse("sort", "popular"))
  }
}

// Function to update URL with current signal values
def updateQueryParams(updateUrl: Map[String, String] => Unit): Unit = {
  val params = Map[String, String]()
    .updated("aspect", aspectRatioSignal.now())
    .updated("dimensions", dimensionsSignal.now())
    .updated("category", categorySignal.now())
    .updated("artist", artistSignal.now())
    .updated("sort", sortBySignal.now())
    .filter(_._2.nonEmpty) // Remove empty values

  updateUrl(params)
}

@main def run(): Unit = {
  // Render the app to the DOM
  render(App, "app")

  // App Component
  def App: FluxusNode = {
    // Parse URL params for initial state
    useEffect(
      () => {
        val initialParams = parseQueryParams()
        initFromQueryParams(initialParams)

        // Handle browser navigation events
        val handler = (_: dom.Event) => {
          val params = parseQueryParams()
          initFromQueryParams(params)
        }

        window.addEventListener("popstate", handler)

        // Cleanup
        () => window.removeEventListener("popstate", handler)
      },
      Seq(), // Empty deps = run once on mount
    )

    // Use signals in component
    val aspectRatio = useSignal(aspectRatioSignal)
    val dimensions  = useSignal(dimensionsSignal)
    val category    = useSignal(categorySignal)
    val artist      = useSignal(artistSignal)
    val sortBy      = useSignal(sortBySignal)

    // Determine if we're showing filtered content
    val isFiltered = aspectRatio.nonEmpty || dimensions.nonEmpty ||
      category.nonEmpty || artist.nonEmpty || sortBy != "popular"

    // Handle filter changes
    def updateFilter(key: String, value: String): Unit = {
      Transaction { _ =>
        key match {
          case "aspect"     => aspectRatioSignal.set(value)
          case "dimensions" => dimensionsSignal.set(value)
          case "category"   => categorySignal.set(value)
          case "artist"     => artistSignal.set(value)
          case "sort"       => sortBySignal.set(value)
        }
      }

      // Update URL
      val currentParams = parseQueryParams()
      val newParams     = if (value.isEmpty) currentParams - key else currentParams + (key -> value)
      updateQueryParams(newParams)
    }

    // Navigation event listener for back/forward buttons
    useEffect(
      () => {
        val handler = (_: dom.Event) => {
          val params = parseQueryParams()
          Transaction { _ =>
            aspectRatioSignal.set(params.getOrElse("aspect", ""))
            dimensionsSignal.set(params.getOrElse("dimensions", ""))
            categorySignal.set(params.getOrElse("category", ""))
            artistSignal.set(params.getOrElse("artist", ""))
            sortBySignal.set(params.getOrElse("sort", "popular"))
          }
        }

        window.addEventListener("popstate", handler)

        // Cleanup
        () => window.removeEventListener("popstate", handler)
      },
      Seq(),
    )

    div(
      Navbar <> (),
      HeroSection <> (),
      FilterBar <> FilterBarProps(
        aspect = aspectRatio,
        dimensions = dimensions,
        category = category,
        artist = artist,
        sortBy = sortBy,
        onFilterChange = updateFilter,
      ),
      if (isFiltered)
        FilteredContent <> FilteredContentProps(
          aspect = aspectRatio,
          dimensions = dimensions,
          category = category,
          artist = artist,
          sortBy = sortBy,
        )
      else
        HomeContent <> (),
      Footer <> (),
    )
  }

  // URL parameter handling
  def parseQueryParams(): Map[String, String] = {
    val queryString = window.location.search.stripPrefix("?")
    if (queryString.isEmpty) Map.empty
    else {
      queryString.split("&").map { param =>
        val parts = param.split("=")
        if (parts.length > 1) parts(0) -> URIUtils.decodeURIComponent(parts(1))
        else parts(0)                  -> ""
      }.toMap
    }
  }

  def updateQueryParams(params: Map[String, String]): Unit = {
    val queryString = params.map { case (key, value) =>
      s"$key=${URIUtils.encodeURIComponent(value)}"
    }.mkString("&")

    val newUrl = if (queryString.isEmpty)
      window.location.pathname
    else
      s"${window.location.pathname}?$queryString"

    window.history.pushState(null, "", newUrl)
  }

  // --- Components ---

  // Navbar Component
  def Navbar = () => {
    div(
      cls := "navbar bg-primary text-primary-content",
      div(
        cls := "navbar-start",
        div(
          cls := "dropdown",
          label(
            "tabindex" := "0",
            cls        := "btn btn-ghost lg:hidden",
            svg(
              xmlns   := "http://www.w3.org/2000/svg",
              cls     := "h-5 w-5",
              fill    := "none",
              viewBox := "0 0 24 24",
              stroke  := "currentColor",
              path(
                "strokeLinecap"  := "round",
                "strokeLinejoin" := "round",
                "strokeWidth"    := "2",
                d                := "M4 6h16M4 12h8m-8 6h16",
              ),
            ),
          ),
          ul(
            "tabindex" := "0",
            cls := "menu menu-sm dropdown-content mt-3 z-[1] p-2 shadow bg-base-100 rounded-box w-52 text-neutral",
            li(a("Home")),
            li(a("Browse")),
            li(a("Artists")),
            li(a("About")),
          ),
        ),
        a(cls := "btn btn-ghost normal-case text-xl", "ChristianWallpapers.art"),
      ),
      div(
        cls := "navbar-center hidden lg:flex",
        ul(
          cls := "menu menu-horizontal px-1",
          li(a("Home")),
          li(a("Browse")),
          li(a("Artists")),
          li(a("About")),
        ),
      ),
      div(
        cls := "navbar-end",
        a(cls := "btn btn-ghost", "Sign In"),
        a(cls := "btn btn-secondary", "Create Account"),
      ),
    )
  }

  // Hero Section Component
  def HeroSection = () => {
    div(
      cls := "hero min-h-96 bg-base-200",
      div(
        cls := "hero-content text-center",
        div(
          cls := "max-w-lg",
          h1(cls := "text-5xl font-bold", "Beautiful Christian Art"),
          p(
            cls := "py-6",
            "Free wallpapers for all your devices, created by talented Christian artists from around the world.",
          ),
          div(
            cls := "join",
            input(cls  := "input input-bordered join-item", placeholder := "Search artwork..."),
            button(cls := "btn btn-primary join-item", "Search"),
          ),
        ),
      ),
    )
  }

  // Filter Bar Component
  case class FilterBarProps(
      aspect: String,
      dimensions: String,
      category: String,
      artist: String,
      sortBy: String,
      onFilterChange: (String, String) => Unit,
  )

  def FilterBar = (props: FilterBarProps) => {
    div(
      cls := "bg-base-200 p-4",
      div(
        cls := "container mx-auto",
        div(
          // Make filters display horizontally with flex and responsive layout
          cls := "flex flex-wrap items-center gap-2 mb-3",

          // Aspect Ratio filter
          // Aspect Ratio filter with inline clear button
          div(
            cls := "flex-1 min-w-[180px]",
            div(
              cls := "relative w-full",
              // Select element with z-index to ensure it's below the clear button
              select(
                cls   := "select select-bordered w-full pr-8", // Added padding-right to make room for the button
                value := props.aspect,
                onChange := ((e: dom.Event) =>
                  props.onFilterChange("aspect", e.target.asInstanceOf[dom.html.Select].value)
                ),
                option(value := "", disabled      := true, selected := props.aspect.isEmpty, "Aspect Ratio"),
                option(value := "16:9", selected  := props.aspect == "16:9", "16:9 (Desktop)"),
                option(value := "9:16", selected  := props.aspect == "9:16", "9:16 (Mobile)"),
                option(value := "4:3", selected   := props.aspect == "4:3", "4:3 (Tablet)"),
                option(value := "3:2", selected   := props.aspect == "3:2", "3:2 (Desktop)"),
                option(value := "16:10", selected := props.aspect == "16:10", "16:10 (Desktop)"),
                option(value := "1:1", selected   := props.aspect == "1:1", "1:1 (Square)"),
              ),
              // Absolutely positioned clear button with higher z-index
              if (props.aspect.nonEmpty)
                button(
                  cls := "absolute right-3 top-1/2 -translate-y-1/2 z-10 h-4 w-4 flex items-center justify-center rounded-full bg-gray-300 text-gray-700 hover:bg-gray-400",
                  onClick := ((e: dom.Event) => {
                    e.stopPropagation() // Prevent triggering select dropdown
                    props.onFilterChange("aspect", "")
                  }),
                  "×",
                )
              else
                null,
            ),
          ),

          // Dimensions filter
          div(
            cls := "flex-1 min-w-[180px]",
            select(
              cls   := "select select-bordered w-full",
              value := props.dimensions,
              onChange := ((e: dom.Event) =>
                props.onFilterChange("dimensions", e.target.asInstanceOf[dom.html.Select].value)
              ),
              option(value := "", disabled          := true, selected := props.dimensions.isEmpty, "Dimensions"),
              option(value := "1920x1080", selected := props.dimensions == "1920x1080", "1920x1080 (FHD)"),
              option(value := "2560x1440", selected := props.dimensions == "2560x1440", "2560x1440 (QHD)"),
              option(value := "3840x2160", selected := props.dimensions == "3840x2160", "3840x2160 (4K)"),
              option(value := "1920x1200", selected := props.dimensions == "1920x1200", "1920x1200"),
              option(value := "1280x720", selected  := props.dimensions == "1280x720", "1280x720 (HD)"),
              option(value := "750x1334", selected  := props.dimensions == "750x1334", "750x1334 (iPhone)"),
            ),
          ),

          // Categories filter
          div(
            cls := "flex-1 min-w-[180px]",
            select(
              cls   := "select select-bordered w-full",
              value := props.category,
              onChange := ((e: dom.Event) =>
                props.onFilterChange("category", e.target.asInstanceOf[dom.html.Select].value)
              ),
              option(value := "", disabled          := true, selected := props.category.isEmpty, "Categories"),
              option(value := "scripture", selected := props.category == "scripture", "Scripture"),
              option(value := "nature", selected    := props.category == "nature", "Nature"),
              option(value := "cross", selected     := props.category == "cross", "Cross"),
              option(value := "worship", selected   := props.category == "worship", "Worship"),
              option(value := "abstract", selected  := props.category == "abstract", "Abstract"),
            ),
          ),

          // Artists filter
          div(
            cls := "flex-1 min-w-[180px]",
            select(
              cls   := "select select-bordered w-full",
              value := props.artist,
              onChange := ((e: dom.Event) =>
                props.onFilterChange("artist", e.target.asInstanceOf[dom.html.Select].value)
              ),
              option(value := "", disabled                := true, selected := props.artist.isEmpty, "Artists"),
              option(value := "all", selected             := props.artist == "all", "All Artists"),
              option(value := "GraceArtistry", selected   := props.artist == "GraceArtistry", "GraceArtistry"),
              option(value := "FaithDesigns", selected    := props.artist == "FaithDesigns", "FaithDesigns"),
              option(value := "BlessedCreative", selected := props.artist == "BlessedCreative", "BlessedCreative"),
              option(value := "DivineCrafts", selected    := props.artist == "DivineCrafts", "DivineCrafts"),
            ),
          ),

          // Sort buttons
          div(
            cls := "flex-none",
            div(
              cls := "join",
              button(
                cls     := s"btn join-item ${if (props.sortBy == "latest") "btn-active" else ""}",
                onClick := (() => props.onFilterChange("sort", "latest")),
                "Latest",
              ),
              button(
                cls     := s"btn join-item ${if (props.sortBy == "popular") "btn-active" else ""}",
                onClick := (() => props.onFilterChange("sort", "popular")),
                "Popular",
              ),
              button(
                cls     := s"btn join-item ${if (props.sortBy == "featured") "btn-active" else ""}",
                onClick := (() => props.onFilterChange("sort", "featured")),
                "Featured",
              ),
            ),
          ),
        ),
      ),
    )
  }

  // Home Content Component
  def HomeContent = () => {
    div(
      cls := "container mx-auto p-4",
      // Featured Section
      div(
        cls := "mb-10",
        div(
          cls := "flex justify-between items-center mb-4",
          h2(cls := "text-2xl font-bold", "Featured Wallpapers"),
          a(href := "#", cls := "link link-primary", "View All"),
        ),
        div(
          cls := "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6",
          MockData.featuredWallpapers.map(wallpaper => WallpaperCard(wallpaper)),
        ),
      ),

      // New Additions Section
      div(
        cls := "mb-10",
        div(
          cls := "flex justify-between items-center mb-4",
          h2(cls := "text-2xl font-bold", "New Additions"),
          a(href := "#", cls := "link link-primary", "View All"),
        ),
        div(
          cls := "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6",
          MockData.newWallpapers.map(wallpaper => WallpaperCard(wallpaper)),
        ),
      ),

      // Popular Artists Section
      div(
        cls := "mb-10",
        div(
          cls := "flex justify-between items-center mb-4",
          h2(cls := "text-2xl font-bold", "Popular Artists"),
          a(href := "#", cls := "link link-primary", "View All Artists"),
        ),
        div(
          cls := "flex flex-wrap gap-4 justify-center",
          MockData.popularArtists.map(artist => ArtistCard(artist)),
        ),
      ),

      // Join as Artist CTA
      div(
        cls := "hero bg-base-200 rounded-lg mb-10",
        div(
          cls := "hero-content flex-col lg:flex-row",
          img(src := "https://placehold.co/400x320", cls := "max-w-sm rounded-lg shadow-2xl"),
          div(
            h2(cls := "text-3xl font-bold", "Share Your Gift With The World"),
            p(
              cls := "py-6",
              "Are you a Christian artist? Join our community and share your artwork with believers around the world. Create a free account today to start contributing.",
            ),
            button(cls := "btn btn-primary", "Become a Contributor"),
          ),
        ),
      ),

      // Categories Section
      div(
        cls := "mb-10",
        div(
          cls := "flex justify-between items-center mb-4",
          h2(cls := "text-2xl font-bold", "Browse by Category"),
        ),
        div(
          cls := "grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4",
          MockData.categories.map(category =>
            div(
              cls := "card bg-base-100 shadow-xl image-full",
              figure(img(src := s"https://placehold.co/300x200?text=${category.name}", alt := category.name)),
              div(
                cls := "card-body justify-end items-center",
                h3(cls := "card-title text-center", category.name),
              ),
            ),
          ),
        ),
      ),

      // Newsletter Section
      div(
        cls := "card bg-primary text-primary-content mb-10",
        div(
          cls := "card-body",
          h2(cls := "card-title", "Stay Updated"),
          p("Subscribe to our newsletter to get notified when new wallpapers are added."),
          div(
            cls := "form-control",
            div(
              cls := "join",
              input(
                typ         := "text",
                placeholder := "Your email address",
                cls         := "input input-bordered join-item text-neutral w-full",
              ),
              button(cls := "btn btn-secondary join-item", "Subscribe"),
            ),
          ),
        ),
      ),
    )
  }

  // Filtered Content Component
  case class FilteredContentProps(
      aspect: String,
      dimensions: String,
      category: String,
      artist: String,
      sortBy: String,
  )

  def FilteredContent = (props: FilteredContentProps) => {
    // Get filtered wallpapers
    val wallpapers = mockFilterWallpapers(props.aspect, props.dimensions, props.category, props.artist, props.sortBy)

    div(
      cls := "container mx-auto p-4",
      div(
        cls := "mb-4",
        h2(cls := "text-2xl font-bold", "Filtered Results"),
        p(s"Showing ${wallpapers.length} wallpapers"),
      ),
      if (wallpapers.isEmpty) {
        div(
          cls := "text-center py-12",
          h3(cls := "text-xl mb-4", "No wallpapers found with these filters"),
          p("Try adjusting your search criteria to find more results."),
        )
      } else {
        div(
          cls := "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6",
          wallpapers.map(wallpaper => WallpaperCard(wallpaper)),
        )
      },
    )
  }

  // Mock function to filter wallpapers
  def mockFilterWallpapers(
      aspect: String,
      dimensions: String,
      category: String,
      artist: String,
      sortBy: String,
  ): Seq[Wallpaper] = {
    var result = MockData.allWallpapers

    // Apply filters
    if (aspect.nonEmpty) {
      result = result.filter(_.aspectRatio == aspect)
    }

    if (dimensions.nonEmpty) {
      result = result.filter(_.dimensions == dimensions)
    }

    if (category.nonEmpty) {
      result = result.filter(_.category == category)
    }

    if (artist.nonEmpty && artist != "all") {
      result = result.filter(_.artist == artist)
    }

    // Apply sorting
    result = sortBy match {
      case "latest"   => result.sortBy(-_.dateAdded.getTime)
      case "popular"  => result.sortBy(-_.downloads)
      case "featured" => result.filter(_.featured).sortBy(-_.downloads)
      case _          => result
    }

    result
  }

  // Wallpaper Card Component
  def WallpaperCard = (wallpaper: Wallpaper) => {
    div(
      cls := "card card-compact bg-base-100 shadow-xl",
      figure(img(src := wallpaper.imageUrl, alt := wallpaper.title)),
      div(
        cls := "card-body",
        h3(cls := "card-title", wallpaper.title),
        div(
          cls := "flex justify-between",
          div(cls := "badge badge-outline", wallpaper.aspectRatio),
          p(cls   := "text-sm", "by ", a(href := "#", cls := "link link-primary", wallpaper.artist)),
        ),
        div(
          cls := "card-actions justify-end",
          button(cls := "btn btn-primary btn-sm", "Download"),
        ),
      ),
    )
  }

  // Artist Card Component
  def ArtistCard = (artist: Artist) => {
    div(
      cls := "card card-compact w-64 bg-base-100 shadow-xl",
      figure(
        cls := "px-10 pt-10",
        div(
          cls := "avatar",
          div(
            cls := "w-24 rounded-full ring ring-primary ring-offset-base-100 ring-offset-2",
            img(src := artist.avatarUrl, alt := s"${artist.name} Avatar"),
          ),
        ),
      ),
      div(
        cls := "card-body items-center text-center",
        h3(cls := "card-title", artist.name),
        p(artist.description),
        div(
          cls := "card-actions",
          button(cls := "btn btn-primary btn-sm", "View Profile"),
        ),
      ),
    )
  }

  // Footer Component
  def Footer = () => {
    footer(
      cls := "footer p-10 bg-neutral text-neutral-content",
      div(
        span(cls := "footer-title", "ChristianWallpapers.art"),
        p("Beautiful Christian art for all your devices.", br(), "Free to download and share."),
        p("© 2025 ChristianWallpapers.art"),
      ),
      div(
        span(cls := "footer-title", "Services"),
        a(cls    := "link link-hover", "Browse"),
        a(cls    := "link link-hover", "Contribute"),
        a(cls    := "link link-hover", "Create Account"),
        a(cls    := "link link-hover", "Advertise"),
      ),
      div(
        span(cls := "footer-title", "Company"),
        a(cls    := "link link-hover", "About"),
        a(cls    := "link link-hover", "Contact"),
        a(cls    := "link link-hover", "FAQ"),
        a(cls    := "link link-hover", "Terms of Use"),
      ),
      div(
        span(cls := "footer-title", "Legal"),
        a(cls    := "link link-hover", "Terms of Service"),
        a(cls    := "link link-hover", "Privacy Policy"),
        a(cls    := "link link-hover", "License"),
        a(cls    := "link link-hover", "Cookie Policy"),
      ),
      div(
        span(cls := "footer-title", "Follow Us"),
        div(
          cls := "grid grid-flow-col gap-4",
          a(
            svg(
              xmlns    := "http://www.w3.org/2000/svg",
              "width"  := "24",
              "height" := "24",
              viewBox  := "0 0 24 24",
              cls      := "fill-current",
              path(
                d := "M24 4.557c-.883.392-1.832.656-2.828.775 1.017-.609 1.798-1.574 2.165-2.724-.951.564-2.005.974-3.127 1.195-.897-.957-2.178-1.555-3.594-1.555-3.179 0-5.515 2.966-4.797 6.045-4.091-.205-7.719-2.165-10.148-5.144-1.29 2.213-.669 5.108 1.523 6.574-.806-.026-1.566-.247-2.229-.616-.054 2.281 1.581 4.415 3.949 4.89-.693.188-1.452.232-2.224.084.626 1.956 2.444 3.379 4.6 3.419-2.07 1.623-4.678 2.348-7.29 2.04 2.179 1.397 4.768 2.212 7.548 2.212 9.142 0 14.307-7.721 13.995-14.646.962-.695 1.797-1.562 2.457-2.549z",
              ),
            ),
          ),
          a(
            svg(
              xmlns    := "http://www.w3.org/2000/svg",
              "width"  := "24",
              "height" := "24",
              viewBox  := "0 0 24 24",
              cls      := "fill-current",
              path(
                d := "M19.615 3.184c-3.604-.246-11.631-.245-15.23 0-3.897.266-4.356 2.62-4.385 8.816.029 6.185.484 8.549 4.385 8.816 3.6.245 11.626.246 15.23 0 3.897-.266 4.356-2.62 4.385-8.816-.029-6.185-.484-8.549-4.385-8.816zm-10.615 12.816v-8l8 3.993-8 4.007z",
              ),
            ),
          ),
          a(
            svg(
              xmlns    := "http://www.w3.org/2000/svg",
              "width"  := "24",
              "height" := "24",
              viewBox  := "0 0 24 24",
              cls      := "fill-current",
              path(
                d := "M9 8h-3v4h3v12h5v-12h3.642l.358-4h-4v-1.667c0-.955.192-1.333 1.115-1.333h2.885v-5h-3.808c-3.596 0-5.192 1.583-5.192 4.615v3.385z",
              ),
            ),
          ),
        ),
      ),
    )
  }
}
