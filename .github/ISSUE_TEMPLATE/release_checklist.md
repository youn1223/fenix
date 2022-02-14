## Overview ##

Firefox for Android roughly follows the [Firefox Gecko release schedule](https://wiki.mozilla.org/Release_Management/Calendar#Calendars).
This means we cut a beta at the end of every two sprints, with a full cycle (~4 weeks) of baking on Beta before going to release. Uplifts must be approved by Release Owner (amedyne).

The [Firefox for Android release schedule](https://docs.google.com/spreadsheets/d/1HotjliSCGOp2nTkfXrxv8qYcurNpkqLWBKbbId6ovTY/edit#gid=0) contains more details related to specific Mobile handoffs.

### Requirements
- JIRA access
- Bugzilla account
- Sentry access

## Release Checklist
There are two releases this covers: the current sprint that is going out to Beta, and the previous Beta that is going to Production.

## Cutting a Beta

- [ ] Review [FeatureFlags](https://github.com/mozilla-mobile/fenix/blob/main/app/src/main/java/org/mozilla/fenix/FeatureFlags.kt) to determine if there are features that need to be enabled (or disabled) for Beta and Production release of Fenix. 
- [ ] Make a new Beta: Follow instructions [here](https://github.com/mozilla-mobile/fenix/wiki/Creating-a-release-branch).
### Bugfix uplifts / Beta Product Integrity 
- [ ] If bugs are considered release blocker then find someone to fix them on main and the milestone branch (cherry-pick / uplift)
    - [ ] Add the uplift request to the appropriate row in the [Uplifts document](https://docs.google.com/spreadsheets/d/1qIvHpcQ3BqJtlzV5T4M1MhbWVxkNiG-ToeYnWEBW4-I/edit#gid=0).
- [ ] If needed, ship a new beta version (e.g. v1.0-beta.2) and follow the submission checklist again.
- [ ] Once there is GREEN QA signoff, file a [release management bugzilla for rollout](https://bugzilla.mozilla.org/show_bug.cgi?id=1664366)
    - [ ] Check Sentry each day for issues on [Firefox Beta](https://sentry.prod.mozaws.net/operations/firefox-beta/) and if nothing concerning, releng bumps releases(5%, 25%, 100%)


### Production Release Candidate 
- Production Release Candidate is captured on the third week of Beta.  


### Production Release
- [ ] The Production Release Candidate is pushed to the Alpha testing track in [Google Play Console](https://play.google.com/console/u/0/developers/7083182635971239206/app/4972519468758466290/releases/overview) by releng team
- [ ] Check Sentry for new crashes. Follow instructions for [Crash Monitoring](https://github.com/mozilla-mobile/fenix/wiki/Crash-Monitoring). File issues and triage.
- [ ] If nothing is concerning, releng officially tags Release Candidate as Production release, bumps releases(5%, 25%, 100%)
