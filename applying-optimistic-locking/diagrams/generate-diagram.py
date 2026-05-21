#!/usr/bin/env python3
import graphviz

CMD_BG, CMD_BORDER, CMD_FONT = "#6699CC", "#336699", "#1a3350"
EVT_BG, EVT_BORDER, EVT_FONT = "#CC8844", "#996633", "#4d2e0f"
STATE_BG, STATE_BORDER = "#557755", "#2d5a2d"
NEG_BG, NEG_BORDER = "#aa4444", "#772222"
CLIENT_BG, CLIENT_BORDER = "#e0e7ff", "#4338ca"
STORE_BG, STORE_BORDER = "#CC8844", "#996633"

ARROW_CMD = "#336699"
ARROW_EVT = "#996633"
ARROW_SR = "#2d5a2d"
START_BG = "#1f2937"
FONT = "Helvetica"


def cmd(g, n, label):
    g.node(n, label, shape="hexagon", style="filled",
           fillcolor=CMD_BG, color=CMD_BORDER, penwidth="2",
           fontname=FONT, fontsize="10", fontcolor=CMD_FONT,
           margin="0.12,0.06")


def evt(g, n, label):
    g.node(n, label, shape="hexagon", style="filled",
           fillcolor=EVT_BG, color=EVT_BORDER, penwidth="2",
           fontname=FONT, fontsize="10", fontcolor=EVT_FONT,
           margin="0.12,0.06")


def state(g, n, label):
    g.node(n, label, shape="parallelogram", style="filled,bold",
           fillcolor=STATE_BG, color=STATE_BORDER, penwidth="3",
           fontname=FONT, fontsize="10", fontcolor="white",
           margin="0.14,0.07")


def neg(g, n, label):
    g.node(n, label, shape="parallelogram", style="filled,bold",
           fillcolor=NEG_BG, color=NEG_BORDER, penwidth="3",
           fontname=FONT, fontsize="10", fontcolor="white",
           margin="0.14,0.07")


def client(g, n, label):
    g.node(n, label, shape="box", style="filled,rounded",
           fillcolor=CLIENT_BG, color=CLIENT_BORDER, penwidth="2",
           fontname=FONT, fontsize="10", margin="0.14,0.07")


def store(g, n, label):
    g.node(n, label, shape="cylinder", style="filled",
           fillcolor=STORE_BG, color=STORE_BORDER, penwidth="2",
           fontname=FONT, fontsize="10", fontcolor="white",
           margin="0.14,0.07")


def cmd_edge(g, a, b):
    g.edge(a, b, color=ARROW_CMD, penwidth="1.5",
           arrowhead="vee", arrowsize="0.8")


def evt_edge(g, a, b):
    g.edge(a, b, color=ARROW_EVT, penwidth="1.5",
           arrowhead="vee", arrowsize="0.8")


def rebuild_edge(g, a, b):
    g.edge(a, b, style="dashed", color=ARROW_SR, penwidth="1.5",
           arrowhead="vee", arrowsize="0.7")


def purchase_book():
    d = graphviz.Digraph("purchase-book", format="svg")
    d.attr(rankdir="TB", bgcolor="white", fontname=FONT,
           pad="0.4", nodesep="0.35", ranksep="0.4",
           label="Purchase Book", labelloc="t", fontsize="14")

    d.node("s0", "", shape="circle", width="0.25", height="0.25",
           style="filled", fillcolor=START_BG, color=START_BG)

    cmd(d, "cmd", "PurchaseBookCommand")
    evt(d, "evt", "BookPurchasedEvent")
    state(d, "st", "Book(isbn, title, authors)")

    cmd_edge(d, "s0", "cmd")
    evt_edge(d, "cmd", "evt")
    rebuild_edge(d, "evt", "st")

    d.render("purchase-book", cleanup=True)
    print("purchase-book.svg generated")


def update_metadata():
    d = graphviz.Digraph("update-metadata", format="svg")
    d.attr(rankdir="TB", bgcolor="white", fontname=FONT,
           pad="0.4", nodesep="0.35", ranksep="0.4",
           label="Update Book Metadata", labelloc="t", fontsize="14")

    state(d, "before", "Book (existing)")
    cmd(d, "cmd", "CorrectBookDetailsCommand")
    evt(d, "evt", "BookDetailsCorrectedEvent")
    state(d, "after", "Book (corrected)")

    cmd_edge(d, "before", "cmd")
    evt_edge(d, "cmd", "evt")
    rebuild_edge(d, "evt", "after")

    d.render("correct-book-details", cleanup=True)
    print("correct-book-details.svg generated")


def conflict():
    d = graphviz.Digraph("conflict", format="svg")
    d.attr(rankdir="TB", bgcolor="white", fontname=FONT,
           pad="0.5", nodesep="0.6", ranksep="0.55",
           label="Concurrent Update Conflict", labelloc="t", fontsize="14")

    INFO_FONT = "#555555"

    state(d, "book", "Book (version N)")

    client(d, "a", "Client A")
    client(d, "b", "Client B")

    d.node("read_a", "reads version N", shape="plain",
           fontname=FONT, fontsize="8", fontcolor=INFO_FONT)
    d.node("read_b", "reads version N", shape="plain",
           fontname=FONT, fontsize="8", fontcolor=INFO_FONT)

    cmd(d, "cmd_a", "CorrectBookDetailsCommand\n(expectedVersion = N)")
    cmd(d, "cmd_b", "CorrectBookDetailsCommand\n(expectedVersion = N)")

    store(d, "es", "ESDB")

    d.node("check_a", "SubjectIsOnEventId(N)\nN == N \u2192 accepted", shape="plain",
           fontname=FONT, fontsize="8", fontcolor=INFO_FONT)
    d.node("check_b", "SubjectIsOnEventId(N)\nN \u2260 N+1 \u2192 rejected", shape="plain",
           fontname=FONT, fontsize="8", fontcolor=INFO_FONT)

    evt(d, "evt_a", "BookDetailsCorrectedEvent")
    state(d, "book_n1", "Book (version N+1)")
    state(d, "ok", "204 No Content")
    neg(d, "fail", "412 Precondition Failed")

    cmd_edge(d, "book", "a")
    cmd_edge(d, "book", "b")

    d.edge("a", "read_a", style="invis")
    d.edge("b", "read_b", style="invis")

    cmd_edge(d, "a", "cmd_a")
    cmd_edge(d, "b", "cmd_b")

    cmd_edge(d, "cmd_a", "es")
    cmd_edge(d, "cmd_b", "es")

    d.edge("es", "check_a", style="invis")
    d.edge("es", "check_b", style="invis")

    evt_edge(d, "es", "evt_a")
    rebuild_edge(d, "evt_a", "book_n1")
    rebuild_edge(d, "book_n1", "ok")

    evt_edge(d, "es", "fail")

    with d.subgraph() as r:
        r.attr(rank="same")
        r.node("a")
        r.node("b")

    with d.subgraph() as r:
        r.attr(rank="same")
        r.node("read_a")
        r.node("read_b")

    with d.subgraph() as r:
        r.attr(rank="same")
        r.node("cmd_a")
        r.node("cmd_b")

    with d.subgraph() as r:
        r.attr(rank="same")
        r.node("check_a")
        r.node("check_b")

    with d.subgraph() as r:
        r.attr(rank="same")
        r.node("ok")
        r.node("fail")

    d.render("conflict", cleanup=True)
    print("conflict.svg generated")


if __name__ == "__main__":
    purchase_book()
    update_metadata()
    conflict()
