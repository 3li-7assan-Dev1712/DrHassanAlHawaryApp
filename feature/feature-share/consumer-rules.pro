# Consumer R8 rules for :feature:feature-share (applied to any app that depends on it).
#
# Reflection audit (2026-09-26): nothing in this module is accessed reflectively.
#  - Hilt/Dagger code is generated and ships its own rules.
#  - Media3 (transformer/effect/exoplayer/muxer) ships consumer rules for its
#    reflective extension loading; the classes used here (BitmapOverlay subclass,
#    DefaultVideoFrameProcessor.Factory, Presentation, OverlayEffect) are all
#    referenced directly.
#  - Share state is never serialised; nav args go through SavedStateHandle as
#    primitives/strings.
# If you add reflection (Class.forName, JSON (de)serialisation of a model,
# getIdentifier for resources, ...), add the matching -keep rule here.
#
# Resources: the burned-in brand assets (logo, Cairo fonts) are protected from
# resource shrinking by res/raw/share_keep.xml, not by rules in this file.
