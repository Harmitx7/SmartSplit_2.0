# Design System Specification: High-Performance Dark Glassmorphism

## 1. Overview & Creative North Star

### Creative North Star: "The Ethereal Ledger"
This design system moves away from the sterile, grid-locked nature of traditional financial apps. It is built on the concept of "The Ethereal Ledger"—a digital space where data doesn't just sit on a screen but floats within a rich, atmospheric environment. 

For the college demographic, "premium" doesn't mean "corporate." It means high-performance, tactile, and visually expressive. We break the "template" look by utilizing **intentional asymmetry** and **overlapping glass surfaces**. By leaning into deep blacks (`#0c0e12`) contrasted with hyper-vibrant, glowing gradients, we create a sense of depth that feels like a physical object. The goal is to make splitting a dinner bill feel as high-end as interacting with a luxury automotive interface.

---

## 2. Colors

### Tonal Foundation
Our palette is rooted in a "Deep Space" black, providing the perfect canvas for our glowing secondary and tertiary accents.

*   **Primary (The Glow):** `primary` (`#f382ff`) and `primary_container` (`#ed69ff`). Use these for high-action touchpoints and critical financial totals.
*   **Secondary (The Fluidity):** `secondary` (`#3adffa`). Represents movement, such as money in transit or active splits.
*   **Tertiary (The Success):** `tertiary` (`#c4ffcd`). Used for settled debts and positive balances.

---

## 3. Typography

The typography strategy is a play between the technical precision of **Space Grotesk** and the modern readability of **Plus Jakarta Sans**.

*   **Display & Headlines (Space Grotesk):** editorial voice. Display 3.5rem for massive balance reveals.
*   **Titles & Body (Plus Jakarta Sans):** functional data. 0.875rem for transaction lists.

---

## 4. UI Components

### Buttons
- **Primary**: Full roundedness. Background: Gradient from primary to primary_dim.
- **Secondary**: Glassmorphic, 40% opacity, 20px blur.

### Cards (The "Smart Split" Card)
- **Style**: No borders, XL (1.5rem) corner radius.
- **Behavior**: On press, sinks (0.98 scale) and blur intensifies.

### Inputs
- **Style**: Minimalist, no bottom line, surface_container_highest background.

### Custom Indicators
- **Split-Sliver Progress Bar**: Thin (2dp) bar for payment status.
- **Glow-Indicators**: Blurred circles behind icons.
