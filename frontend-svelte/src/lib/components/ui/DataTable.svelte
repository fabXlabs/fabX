<script lang="ts" generics="TData, TValue">
	import {
		type ColumnDef,
		type ColumnFiltersState,
		getCoreRowModel,
		getFilteredRowModel,
		getSortedRowModel,
		type SortingState,
		type VisibilityState
	} from '@tanstack/table-core';
	import { createSvelteTable, FlexRender } from '$lib/components/ui/data-table/';
	import type { Snippet } from 'svelte';
	// noinspection ES6UnusedImports
	import * as Table from '$lib/components/ui/table';
	// noinspection ES6UnusedImports
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu/index.js';
	import { Button } from '$lib/components/ui/button';
	import { Input } from '$lib/components/ui/input';
	import { SlidersHorizontal, XIcon } from 'lucide-svelte';

	type DataTableProps<TData, TValue> = {
		columns: ColumnDef<TData, TValue>[];
		data: TData[];
		initialColumnVisibility: Record<string, boolean>;
		initialSortingState: SortingState;
		onRowSelect?: ((data: TData) => void) | null;
		breadCrumbs: Snippet | null;
		/* eslint-disable  @typescript-eslint/no-explicit-any */
		columnFilterSnippet: Snippet<[{ table: any }]> | null;
		addButton?: Snippet | null;
	};

	let {
		data,
		columns,
		initialColumnVisibility,
		initialSortingState,
		onRowSelect = null,
		breadCrumbs = null,
		columnFilterSnippet = null,
		addButton = null
	}: DataTableProps<TData, TValue> = $props();

	let rowCursor = $derived.by(() => {
		if (onRowSelect) {
			return 'cursor-pointer';
		} else {
			return 'cursor-default';
		}
	});

	let columnVisibility = $state<VisibilityState>(initialColumnVisibility);
	/* eslint-disable  @typescript-eslint/no-explicit-any */
	let globalFilter = $state<any>([]);
	let columnFilters = $state<ColumnFiltersState>([]);
	let sorting = $state<SortingState>(initialSortingState);

	const table = createSvelteTable({
		get data() {
			return data;
		},
		columns,
		getCoreRowModel: getCoreRowModel(),
		getFilteredRowModel: getFilteredRowModel(),
		getSortedRowModel: getSortedRowModel(),
		onColumnVisibilityChange: (updater) => {
			if (typeof updater === 'function') {
				columnVisibility = updater(columnVisibility);
			} else {
				columnVisibility = updater;
			}
		},
		onGlobalFilterChange: (updater) => {
			if (typeof updater === 'function') {
				globalFilter = updater(globalFilter);
			} else {
				globalFilter = updater;
			}
		},
		onColumnFiltersChange: (updater) => {
			if (typeof updater === 'function') {
				columnFilters = updater(columnFilters);
			} else {
				columnFilters = updater;
			}
		},
		onSortingChange: (updater) => {
			if (typeof updater === 'function') {
				sorting = updater(globalFilter);
			} else {
				sorting = updater;
			}
		},
		state: {
			get columnVisibility() {
				return columnVisibility;
			},
			get globalFilter() {
				return globalFilter;
			},
			get columnFilters() {
				return columnFilters;
			},
			get sorting() {
				return sorting;
			}
		},
		globalFilterFn: 'includesString'
	});

	let isFiltered = $derived(table.getState().columnFilters.length > 0);
</script>

{#if breadCrumbs}
	<div class="container mt-5 max-w-(--breakpoint-2xl) px-4 sm:px-8">
		<div class="mx-4 sm:mx-0">
			{@render breadCrumbs()}
		</div>
	</div>
{/if}

<div class="container max-w-(--breakpoint-2xl) px-0 sm:px-8">
	<div class="items-top mx-4 flex justify-between py-4 sm:mx-0">
		<div class="flex flex-wrap items-center gap-y-2 md:flex-nowrap">
			<Input
				placeholder="Search..."
				value=""
				onchange={(e) => {
					table.setGlobalFilter(String(e.currentTarget.value));
				}}
				oninput={(e) => {
					table.setGlobalFilter(String(e.currentTarget.value));
				}}
				class="mr-2 max-w-sm"
				autocorrect="off"
			/>
			{#if columnFilterSnippet}
				{@render columnFilterSnippet({ table })}
				{#if isFiltered}
					<Button
						variant="ghost"
						onclick={() => table.resetColumnFilters()}
						class="h-8 px-2 normal-case lg:px-3"
					>
						Reset
						<XIcon />
					</Button>
				{/if}
			{/if}
		</div>
		<div class="flex flex-nowrap">
			<DropdownMenu.Root>
				<DropdownMenu.Trigger>
					{#snippet child({ props })}
						<Button {...props} variant="outline" class="auto mr-2 normal-case">
							<SlidersHorizontal />
							<div>View</div>
						</Button>
					{/snippet}
				</DropdownMenu.Trigger>
				<DropdownMenu.Content align="end">
					<DropdownMenu.Label>Toggle columns</DropdownMenu.Label>
					<DropdownMenu.Separator />
					{#each table.getAllColumns().filter((col) => col.getCanHide()) as column (column.id)}
						<DropdownMenu.CheckboxItem
							class="capitalize"
							bind:checked={() => column.getIsVisible(), (v) => column.toggleVisibility(!!v)}
						>
							{column.columnDef.header || column.id}
						</DropdownMenu.CheckboxItem>
					{/each}
				</DropdownMenu.Content>
			</DropdownMenu.Root>
			{#if addButton}
				{@render addButton()}
			{/if}
		</div>
	</div>
	<div class="border sm:rounded-md">
		<Table.Root>
			<Table.Header>
				{#each table.getHeaderGroups() as headerGroup (headerGroup.id)}
					<Table.Row>
						{#each headerGroup.headers as header (header.id)}
							<Table.Head>
								{#if !header.isPlaceholder}
									<FlexRender
										content={header.column.columnDef.header}
										context={header.getContext()}
									/>
								{/if}
							</Table.Head>
						{/each}
					</Table.Row>
				{/each}
			</Table.Header>
			<Table.Body>
				{#each table.getRowModel().rows as row (row.id)}
					<Table.Row
						data-state={row.getIsSelected() && 'selected'}
						class={rowCursor}
						onclick={() => onRowSelect?.(row.original)}
					>
						{#each row.getVisibleCells() as cell (cell.id)}
							<Table.Cell>
								<FlexRender content={cell.column.columnDef.cell} context={cell.getContext()} />
							</Table.Cell>
						{/each}
					</Table.Row>
				{:else}
					<Table.Row>
						<Table.Cell colspan={columns.length} class="h-24 text-center">No results</Table.Cell>
					</Table.Row>
				{/each}
			</Table.Body>
		</Table.Root>
	</div>
</div>
