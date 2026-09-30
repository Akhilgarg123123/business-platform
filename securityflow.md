Let's build this from the very bottom, assuming you've never touched Spring Security before.

## The problem, restated simply

Your server has data belonging to different business owners. When a request like `GET /customers` arrives, your server needs to answer two questions before doing anything else:
1. **Is this request coming from someone who's actually logged in?**
2. **Which specific logged-in person is it?** (so you know whose customers to show)

Everything in this system — filters, tokens, security context — exists purely to answer those two questions, consistently, on every single request, automatically.

## Idea 1: A "checkpoint line" before your actual code runs

Imagine your application like a building with many rooms (`/customers`, `/suppliers`, `/expenses`, etc. — your controllers). Spring Security works like a **security checkpoint placed in the hallway**, before anyone reaches any room. Every single visitor (every HTTP request) must pass through this hallway first.

This hallway isn't one single checkpoint — it's a **chain of checkpoints**, each one checking something different, one after another. This chain is called the **filter chain**. Your `JwtAuthFilter` is one checkpoint in that chain. There are others built into Spring Security itself that you didn't write, running before and after yours.

Crucially: **a filter doesn't have to reject anyone.** A filter can simply observe, note something down, and wave the visitor through to the next checkpoint. That's exactly what `JwtAuthFilter` does — it never itself says "denied." It just tries to figure out *who* the visitor is, if it can, and passes them along either way.

## Idea 2: A visitor badge that gets stamped, not looked up in a logbook

Here's the part that's genuinely different from how you might picture "checking if someone's logged in." There is no logbook the server checks — no "list of currently logged-in users" sitting in a database or in memory that says "Ramesh is logged in right now."

Instead, think of it like this: when Ramesh first logged in (via `/auth/login`), your server handed him a **badge** — the JWT. This badge is signed with a special stamp only your server knows how to make (the `app.jwt.secret` we discussed). The badge itself says, in plain readable text if you unfold it, "this is Ramesh's phone number, and this badge expires at such-and-such time."

Every time Ramesh sends a request afterward, he has to show this badge (in the `Authorization: Bearer ...` header). The checkpoint (`JwtAuthFilter`) doesn't look Ramesh up anywhere — it just:
1. Takes the badge
2. Checks the stamp is genuine (nobody could have faked a stamp without knowing the secret)
3. Checks the badge hasn't expired
4. Reads the phone number written on the badge
5. Looks that phone number up in your actual `users` table, just to fetch a few details about them (this is the *only* database lookup involved, and it's not "checking if they're logged in" — it's just "fetching who this badge belongs to")

There's no "logged in" flag stored anywhere. A badge is either currently valid (right signature, not expired) or it isn't — that's the entirety of "being logged in," each and every request, freshly re-checked from scratch every time.

## Idea 3: A sticky note that only lasts for one visit

Once the checkpoint (`JwtAuthFilter`) confirms the badge is real and figures out who it belongs to, it needs a way to **tell everyone else further down the hallway** — the other checkpoints, and eventually the controller in the room itself — "hey, this particular visitor has been confirmed as Ramesh."

This is what `SecurityContextHolder.getContext().setAuthentication(authToken)` is doing. Picture it as **sticking a note onto this one specific visitor**, readable by anyone else they pass on their way through the hallway, saying "confirmed: this is Ramesh." Every other checkpoint after this one, and your controller at the very end, can glance at that note without needing to redo any of the badge-checking work themselves.

Critically, this note is **temporary and personal to this one visit** — the moment this request finishes and the response goes back, the note is thrown away. If Ramesh sends another request a second later, the entire badge-checking process happens completely fresh, from scratch, and a brand new (identical-content) note gets written for that new request. Nothing persists between requests except the badge itself, which Ramesh keeps holding onto and re-presents every time.

## Idea 4: The rulebook that decides who needs a badge at all

Separately from the checkpoint that reads badges, there's a **rulebook** — `SecurityConfig` — that decides which rooms require a confirmed badge before entering, and which don't.
```java
.requestMatchers("/auth/**").permitAll()
.anyRequest().authenticated()
```
This rulebook says: "the rooms behind `/auth/register` and `/auth/login` — anyone can walk in, badge or not, since that's literally where people go to *get* a badge in the first place. Every other room requires a note on file saying 'confirmed: this visitor is someone.'"

This is why `JwtAuthFilter` itself never rejects anyone — it just tries to write a note if it can. The actual *rejection* ("sorry, you can't go past this point without a note") is enforced afterward, by this separate rulebook, checking whether a note was successfully written by the time the visitor reaches this point.

## Putting all four ideas together, as one full walkthrough

**When Ramesh first logs in:**
1. He sends his phone number + password to `/auth/login`
2. The rulebook says this room is open to everyone — no badge needed to get in here
3. `AuthService` checks his password is correct
4. If correct, `JwtUtil` prints him a brand new badge (a signed JWT), containing his phone number and an expiry time
5. This badge is handed back to him in the response

**On every request Ramesh makes afterward (e.g. `GET /customers`):**
1. Ramesh's frontend automatically attaches his badge in the `Authorization` header
2. The request enters the hallway (filter chain)
3. `JwtAuthFilter` is the checkpoint that examines the badge:
    - Is there a badge at all? (checks the header exists and starts with "Bearer ")
    - Is the stamp genuine and not expired? (`isTokenValid`)
    - If yes to both: reads the phone number off the badge, fetches Ramesh's details, and writes the sticky note ("confirmed: this is Ramesh") for this one request
    - If no: writes no note at all, and just lets the request continue anyway (this filter never itself blocks anything)
4. The request reaches the rulebook (`SecurityConfig`'s authorization rule): "does `/customers` require a note? Yes. Is there a note on this visitor? Yes, it says Ramesh." → allowed through
5. The request finally reaches `CustomerController`, which can now ask "who is the current user" (by reading that same sticky note) and knows to only show Ramesh's own customers

**If someone sends a request with no badge, or a fake/expired one, to `/customers`:**
1. `JwtAuthFilter` either finds nothing to check, or finds the badge is invalid — either way, no note gets written
2. The request reaches the rulebook: "does `/customers` require a note? Yes. Is there a note? No." → rejected, with a `401 Unauthorized` response, never even reaching your controller code

## Why this design, instead of something simpler

You might reasonably ask: why not just have the server remember "Ramesh is logged in" the moment he logs in, and check that memory on every request? That's actually how many older systems work (session-based auth) — but it requires the server to **remember every logged-in person**, which becomes a real burden once you have many users or multiple servers sharing the load. The badge approach means the server remembers nothing between requests — each badge carries everything needed to verify itself, on the spot, every single time, which is what "stateless" has meant every time it's come up in this conversation.

## One sentence summary

A signed badge proves who you are without the server needing to remember you; one checkpoint reads and verifies that badge fresh on every request and writes a temporary note saying who it belongs to; a separate rulebook decides which rooms require that note before letting a request through; and your controllers, at the very end, simply read that note to know whose data to show.

Does this land clearly? If so, we can move to actually creating these 13 auth files and testing register/login in Postman — that'll make all of this concrete rather than conceptual.