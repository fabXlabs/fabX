<script lang="ts">
  // noinspection ES6UnusedImports
  import * as Sheet from '$lib/components/ui/sheet/index.js';
  import { UserRoundPlus } from 'lucide-svelte';
  import { Button } from '$lib/components/ui/button';
  import { Label } from '$lib/components/ui/label';
  import { Input } from '$lib/components/ui/input';
  import { addUser } from '$lib/api/users';
  import { goto } from '$app/navigation';
  import { base } from '$app/paths';
  import type { FabXError } from '$lib/api/model/error';
  import ErrorText from '$lib/components/ErrorText.svelte';

  let sheetOpen = $state(false);

  let firstName = $state('');
  let lastName = $state('');
  let wikiName = $state('');

  let error: FabXError | null = $state(null);

  async function submit() {
    error = null;

    const res = await addUser({
      firstName,
      lastName,
      wikiName
    }).catch(e => {
      error = e;
      return '';
    });

    if (res) {
      sheetOpen = false;
      await goto(`${base}/admin/user/${res}`);
    }
  }
</script>
<Sheet.Root bind:open={sheetOpen}>
  <Sheet.Trigger
          class="mr-2 px-0 text-center text-base hover:bg-transparent focus-visible:bg-transparent focus-visible:ring-0 focus-visible:ring-offset-0 outline-hidden">
    <Button class="normal-case">
      <UserRoundPlus />
      Add User
    </Button>
  </Sheet.Trigger>
  <Sheet.Content side="right" class="flex flex-col">
    <Sheet.Header>
      <Sheet.Title>Add User</Sheet.Title>
    </Sheet.Header>
    <form onsubmit={submit}>
      <div class="grid gap-4 py-4">
        <div class="grid grid-cols-4 items-center gap-4">
          <Label for="firstName" class="text-right">First Name</Label>
          <Input id="firstName" class="col-span-3" bind:value={firstName} />
        </div>
        <div class="grid grid-cols-4 items-center gap-4">
          <Label for="lastName" class="text-right">Last Name</Label>
          <Input id="lastName" class="col-span-3" bind:value={lastName} />
        </div>
        <div class="grid grid-cols-4 items-center gap-4">
          <Label for="wikiName" class="text-right">Wiki Name</Label>
          <Input id="wikiName" class="col-span-3" bind:value={wikiName} />
        </div>
      </div>

      <ErrorText {error} />

      <Sheet.Footer>
        <Button type="submit" variant="outline">Add</Button>
      </Sheet.Footer>
    </form>
  </Sheet.Content>
</Sheet.Root>