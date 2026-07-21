# Design QA

## Comparison Target

- Source visual truth:
  - `D:\ProgramData\PublicProjects\Onlinelottery\Imports\ReferenceImage2.jpg`
  - `D:\ProgramData\PublicProjects\Onlinelottery\Imports\ReferenceImage3.jpg`
  - `D:\ProgramData\PublicProjects\Onlinelottery\Imports\ReferenceImage4.jpg`
- Implementation screenshots:
  - `qa/home-final.png`
  - `qa/matches-final.png`
  - `qa/profile.png`
  - `qa/bet-slip.png`
  - `qa/recharge-modal.png`
- Side-by-side evidence:
  - `qa/compare-home-final.png`
  - `qa/compare-matches-final.png`
  - `qa/compare-profile.png`
- Viewport: 390 x 844
- States: home default, football matches default, profile default, one selected odd with bet slip open, recharge sheet open

## Full-view Comparison Evidence

The three source screens and matching implementation captures were normalized to the same 390 x 844 viewport and placed side-by-side. The final comparisons preserve the source hierarchy: blue/white visual system, hero and account card on home, compact match cards with green live states, white profile cards, and persistent three-item navigation.

## Focused Region Comparison Evidence

The home header/banner/account-card region and the match team/score/odds rows were inspected at original composite resolution. Separate focused crops were not needed because the 780 x 844 side-by-side composites keep typography, spacing, icon placement, and odds controls legible.

## Required Fidelity Surfaces

- Fonts and typography: Noto Sans SC with Microsoft YaHei fallback matches the compact Chinese sans-serif hierarchy. Weights and truncation remain readable at 390 px.
- Spacing and layout rhythm: 14 px page gutters, rounded white cards, compact vertical gaps, fixed status and bottom navigation, and internal scrolling match the sources without horizontal overflow.
- Colors and visual tokens: primary blue, live green, alert red, amber, pale gray page background, and low-elevation card shadows map consistently to the references.
- Image quality and asset fidelity: the supplied sports campaign art is reused from the reference at native source quality. Standard UI and lottery symbols use Phosphor icons; no placeholder, emoji, handmade SVG, or CSS illustration is used.
- Copy and content: visible Chinese labels, balances, match names, scores, dates, odds, account actions, and record statuses follow the supplied references with privacy masking on the sample phone number.

## Findings

- No actionable P0, P1, or P2 findings remain.
- P3: exact lottery tile artwork and club crests were unavailable as standalone assets, so the prototype uses consistent library icons. Replace them when licensed production assets are supplied.
- P3: the prototype shows the primary win/draw/loss odds row; the secondary handicap market from the source remains a later functional expansion.

## Comparison History

### Iteration 1

- P2: the home title wrapped to two lines and shifted the banner start. Fixed by using a single-line title and rebalancing header/banner heights.
- P2: match crests were stacked above team names, making cards too tall and reducing visible match density. Fixed by placing icons inline with names and tightening card rhythm.

### Iteration 2

- Post-fix evidence: `qa/compare-home-final.png` and `qa/compare-matches-final.png`.
- Result: source hierarchy and viewport density now align; no actionable P0/P1/P2 mismatch remains.

## Interaction Verification

- Bottom navigation switches among home, matches, and profile.
- Sport/type/date controls update visible state.
- Selecting an odd opens the bet slip and visually marks the choice.
- Stake controls update the projected return.
- Simulated bet submission reaches a visible success state.
- Recharge opens a functional amount sheet with presets and confirmation.
- Balance visibility and prototype feedback toasts are interactive.
- Browser console errors/warnings checked: none.
- Layout metrics: 390 px viewport, 390 px body width, no horizontal overflow.

## Implementation Checklist

- [x] Match reference layout and core visual tokens.
- [x] Implement three primary screens.
- [x] Implement the core match-to-bet flow.
- [x] Implement safe simulated money interactions.
- [x] Verify build, browser rendering, console, and viewport bounds.

final result: passed
