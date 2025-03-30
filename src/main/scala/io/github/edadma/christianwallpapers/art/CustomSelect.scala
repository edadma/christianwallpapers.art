// CustomSelect.scala (add this to your package)
package io.github.edadma.christianwallpapers.art

import io.github.edadma.fluxus._
import org.scalajs.dom
import scala.scalajs.js.timers.setTimeout

// Define the option type
case class SelectOption(value: String, label: String)

// Define props as a case class
case class CustomSelectProps(
    value: String,
    options: Seq[SelectOption],
    placeholder: String,
    onChange: String => Unit,
    className: String = "",
)

def CustomSelect = (props: CustomSelectProps) => {
  val (isOpen, setIsOpen, _) = useState(false)
  val dropdownRef            = useRef[dom.html.Div]()

  // Close dropdown when clicking outside
  useEffect(
    () => {
      val handleClickOutside = (e: dom.Event) => {
        if (dropdownRef.current != null && !dropdownRef.current.contains(e.target.asInstanceOf[dom.Node])) {
          setIsOpen(false)
        }
      }

      dom.document.addEventListener("mousedown", handleClickOutside)

      // Cleanup
      () => dom.document.removeEventListener("mousedown", handleClickOutside)
    },
    Seq(),
  )

  // Find the label for the current value
  val selectedLabel = props.options.find(_.value == props.value).map(_.label).getOrElse(props.placeholder)

  div(
    cls := s"relative w-full ${props.className}",
    ref := dropdownRef,

    // Select button
    button(
      typ     := "button",
      cls     := "select select-bordered w-full flex items-center justify-between text-left h-12 pl-4 pr-3",
      onClick := (() => setIsOpen(!isOpen)),

      // Text content
      div(
        cls := "flex-grow truncate",
        span(
          cls := s"${if (props.value.isEmpty) "text-opacity-70" else ""}",
          selectedLabel,
        ),
      ),

      // Control area (clear button + caret)
      div(
        cls := "flex items-center ml-1 gap-1",
        // Clear button - only show when there's a value
        if (props.value.nonEmpty)
          button(
            typ := "button",
            cls := "h-6 w-6 rounded-full bg-gray-300 text-gray-700 flex items-center justify-center hover:bg-gray-400",
            onClick := ((e: dom.Event) => {
              e.stopPropagation() // Don't trigger the parent button
              props.onChange("")
            }),
            "×",
          )
        else null,

        // Dropdown caret
        svg(
          xmlns   := "http://www.w3.org/2000/svg",
          cls     := "h-4 w-4 ml-1",
          fill    := "none",
          viewBox := "0 0 24 24",
          stroke  := "currentColor",
          path(
            "strokeLinecap"  := "round",
            "strokeLinejoin" := "round",
            "strokeWidth"    := "2",
            d                := "M19 9l-7 7-7-7",
          ),
        ),
      ),
    ),

    // Dropdown menu
    if (isOpen)
      div(
        cls := "absolute z-50 w-full mt-1 bg-base-100 shadow-lg rounded-lg overflow-hidden max-h-60 overflow-y-auto",
        div(
          cls := "py-1",
          // Render each option
          props.options.map(option =>
            button(
              key := option.value,
              typ := "button",
              cls := s"w-full px-4 py-2 text-left hover:bg-base-200 ${
                  if (props.value == option.value) "bg-base-300" else ""
                }",
              onClick := (() => {
                props.onChange(option.value)
                setIsOpen(false)
              }),
              option.label,
            ),
          ),
        ),
      )
    else null,
  )
}
