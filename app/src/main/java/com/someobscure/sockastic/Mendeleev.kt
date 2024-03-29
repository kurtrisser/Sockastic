package com.someobscure.sockastic

class Mendeleev  {
  companion object {
    private const val col_name = 0
    private const val col_symbol = 1
    private const val col_number = 2

    internal val elements = ArrayList<AtomicElement>()
    private val raw = """
Actinium Ac 89
Aluminum Al 13
Americium Am 95
Antimony Sb 51
Argon Ar 18
Arsenic As 33
Astatine At 85
Barium Ba 56
Berkelium Bk 97
Beryllium Be 4
Bismuth Bi 83
Bohrium Bh 107
Boron B 5
Bromine Br 35
Cadmium Cd 48
Calcium Ca 20
Californium Cf 98
Carbon C 6
Cerium Ce 58
Cesium Cs 55
Chlorine Cl 17
Chromium Cr 24
Cobalt Co 27
Copernicium Cn 112
Copper Cu 29
Curium Cm 96
Darmstadtium Ds 110
Dubnium Db 105
Dysprosium Dy 66
Einsteinium Es 99
Erbium Er 68
Europium Eu 63
Fermium Fm 100
Flerovium Fl 114
Fluorine F 9
Francium Fr 87
Gadolinium Gd 64
Gallium Ga 31
Germanium Ge 32
Gold Au 79
Hafnium Hf 72
Hassium Hs 108
Helium He 2
Holmium Ho 67
Hydrogen H 1
Indium In 49
Iodine I 53
Iridium Ir 77
Iron Fe 26
Krypton Kr 36
Lanthanum La 57
Lawrencium Lr 103
Lead Pb 82
Lithium Li 3
Livermorium Lv 116
Lutetium Lu 71
Magnesium Mg 12
Manganese Mn 25
Meitnerium Mt 109
Mendelevium Md 101
Mercury Hg 80
Molybdenum Mo 42
Moscovium Mc 115
Neodymium Nd 60
Neon Ne 10
Neptunium Np 93
Nickel Ni 28
Nihonium Nh 113
Niobium Nb 41
Nitrogen N 7
Nobelium No 102
Oganesson Og 118
Osmium Os 76
Oxygen O 8
Palladium Pd 46
Phosphorus P 15
Platinum Pt 78
Plutonium Pu 94
Polonium Po 84
Potassium K 19
Praseodymium Pr 59
Promethium Pm 61
Protactinium Pa 91
Radium Ra 88
Radon Rn 86
Rhenium Re 75
Rhodium Rh 45
Roentgenium Rg 111
Rubidium Rb 37
Ruthenium Ru 44
Rutherfordium Rf 104
Samarium Sm 62
Scandium Sc 21
Seaborgium Sg 106
Selenium Se 34
Silicon Si 14
Silver Ag 47
Sodium Na 11
Strontium Sr 38
Sulfur S 16
Tantalum Ta 73
Technetium Tc 43
Tellurium Te 52
Tennessine Ts 117
Terbium Tb 65
Thallium Tl 81
Thorium Th 90
Thulium Tm 69
Tin Sn 50
Titanium Ti 22
Tungsten W 74
Uranium U 92
Vanadium V 23
Xenon Xe 54
Ytterbium Yb 70
Yttrium Y 39
Zinc Zn 30
Zirconium Zr 40
""".trimIndent()


    init {
      println("Mendeleev companion object init: ")
      if (elements.isEmpty()) {
        raw.reader()
          .forEachLine { s ->
            val col = s.split(' ')
            elements.add(
              AtomicElement(
                col[col_number].trim().toInt(),  // number
                col[col_symbol].trim(),          // symbol
                col[col_name].trim()             // name
              )
            )
          }
        println("Initialized ${elements.size} elements. ")
      }
    }
  }

  fun lookup(request: String): String {
    if (request.trim().toIntOrNull() == null) {
      // 'tis not a number.
//      if (request.trim().length > 2) { // It's an element name.
        elements.forEach { e -> if ((if(request.trim().length > 2) e.name else e.symbol).toLowerCase() == request.trim().toLowerCase()) return "${e.symbol} ${e.number} ${e.name}" }
//        elements.forEach { e -> if (e.name.toLowerCase() == request.trim().toLowerCase()) return "${e.symbol} ${e.number} ${e.name}" }
//      } else {
//        elements.forEach { e -> if (e.symbol.toLowerCase() == request.trim().toLowerCase()) return "${e.symbol} ${e.number} ${e.name}" }
//      }
    } else {
      // 'tis a number.
      elements.forEach { e -> if (e.number == request.trim().toInt()) return "${e.symbol} ${e.number} ${e.name}" }
    }
    return ""
  }

  fun usage(): String {
    return "To lookup an element from the periodic table, provide either a name, a symbol (i.e., 'Li', 'C', etc.), or an atomic number. "
  }

/* https://www.lenntech.com/periodic/name/alphabetic.htm#ixzz5qZscMJKW */
}